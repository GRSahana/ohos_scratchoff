package com.jackpocket.scratchoff;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentTreeObserver;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.render.BlendMode;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.render.Texture;
import ohos.agp.utils.Color;
import ohos.hiviewdfx.HiLog;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;

import com.jackpocket.scratchoff.paths.ScratchPathManager;
import com.jackpocket.scratchoff.paths.ScratchPathPoint;
import com.jackpocket.scratchoff.paths.ScratchPathPointsAggregator;
import com.jackpocket.scratchoff.tools.ViewGroupVisibilityController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScratchableLayoutDrawer implements ScratchPathPointsAggregator, Animator.StateChangedListener {

    enum State {
        UNATTACHED,
        PREPARING,
        SCRATCHABLE,
        CLEARING,
        CLEARED
    }

    public interface Delegate {
        public void onScratchableLayoutAvailable(int width, int height);
    }

    private WeakReference<Component> scratchView = new WeakReference<>(null);

    private State state = State.UNATTACHED;
    private Canvas pathStrippedCanvas;
    private PixelMapHolder pathStrippedImage;

    private final WeakReference<Delegate> delegate;

    private Paint clearPaint = new Paint();

    private int clearAnimationInterpolator = Animator.CurveType.LINEAR;
    private long clearAnimationDurationMs = 1000;

    private final ViewGroupVisibilityController visibilityController = new ViewGroupVisibilityController();

    private final ArrayList<ScratchPathPoint> pendingPathPoints = new ArrayList<ScratchPathPoint>();
    private final ScratchPathManager pathManager = new ScratchPathManager();

    private Long activeClearTag = 0L;

    public ScratchableLayoutDrawer(Delegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    @SuppressWarnings("WeakerAccess")
    public ScratchableLayoutDrawer attach(
            ScratchoffController controller,
            ComponentContainer scratchView,
            ComponentContainer behindView) {

        return attach(
                controller.getTouchRadiusPx(),
                scratchView,
                behindView);
    }

    public ScratchableLayoutDrawer attach(
            int touchRadiusPx,
            final ComponentContainer scratchView,
            final ComponentContainer behindView) {

        synchronized (pathManager) {
            this.scratchView = new WeakReference<>(scratchView);
            this.state = State.PREPARING;
            this.clearPaint = createClearPaint(touchRadiusPx);
            this.activeClearTag = System.currentTimeMillis();
            HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer attach()");
            Map<Integer, Object> tag = new HashMap<>();
            tag.put(1, "ScratchViewTag");
            scratchView.setTag(tag);
            //scratchView.abortAnimation();
            scratchView.setVisibility(Component.VISIBLE);
            //scratchView.setWillNotDraw(false);

            visibilityController.showChildren(scratchView);

            scratchView.invalidate();

            enqueueViewInitializationOnGlobalLayout(scratchView, behindView);

            return this;
        }
    }

    protected Paint createClearPaint(int touchRadiusPx) {
        Paint paint = ScratchPathManager.createBaseScratchoffPaint(touchRadiusPx);
        paint.setAlpha(0xFF);
        paint.setAntiAlias(true);
        paint.setBlendMode(BlendMode.CLEAR);

        return paint;
    }

    protected void enqueueViewInitializationOnGlobalLayout(final ComponentContainer scratchView, final Component behindView) {
        if (behindView == null) {
            HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer enqueueViewInitializationOnGlobalLayout()");
            enqueueScratchableViewInitializationOnGlobalLayout(scratchView);

            return;
        }

        addGlobalLayoutRequest(
                behindView,
                new Runnable() {
                    public void run() {
                        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer enqueueViewInitializationOnGlobalLayout() inside run");
                        performLayoutDimensionMatching(scratchView, behindView);
                        enqueueScratchableViewInitializationOnGlobalLayout(scratchView);
                    }
                });
    }

    @SuppressWarnings("WeakerAccess")
    protected void performLayoutDimensionMatching(final Component scratchView, final Component behindView) {
        ComponentContainer.LayoutConfig params = scratchView.getLayoutConfig();
        params.width = behindView.getWidth();
        params.height = behindView.getHeight();

        scratchView.setLayoutConfig(params);
    }

    @SuppressWarnings("WeakerAccess")
    protected void enqueueScratchableViewInitializationOnGlobalLayout(final ComponentContainer scratchView) {
        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer enqueueScratchableViewInitializationOnGlobalLayout()2");
        addGlobalLayoutRequest(
                scratchView,
                new Runnable() {
                    public void run() {
                        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer enqueueScratchableViewInitializationOnGlobalLayout()2 run");
                        initializeLaidOutScratchableView(scratchView);
                    }
                });
    }

    protected void initializeLaidOutScratchableView(final ComponentContainer scratchView) {
        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer initializeLaidOutScratchableView() bfr");
        synchronized (pathManager) {
            this.pathStrippedImage = createBitmapFromScratchableView(scratchView);
            HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer initializeLaidOutScratchableView() "+pathStrippedImage);
            this.pathStrippedCanvas = new Canvas(new Texture(pathStrippedImage.getPixelMap()));

            ShapeElement backgroundShape = new ShapeElement();
            backgroundShape.setShape(ShapeElement.RECTANGLE);
            backgroundShape.setRgbColor(RgbColor.fromArgbInt(Color.TRANSPARENT.getValue()));
            scratchView.setBackground(backgroundShape);

            visibilityController.hideChildren(scratchView);

            Delegate delegate = this.delegate.get();
            if (delegate != null) {
                HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer initializeLaidOutScratchableView() delegate not null");
                delegate.onScratchableLayoutAvailable(
                        pathStrippedImage.getPixelMap().getImageInfo().size.width,
                        pathStrippedImage.getPixelMap().getImageInfo().size.height);
            }

            this.state = State.SCRATCHABLE;

            addPendingScratchPathPointsAndClear();
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected PixelMapHolder createBitmapFromScratchableView(final ComponentContainer scratchView) {
        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer createBitmapFromScratchableView() bfr");

        PixelMap.InitializationOptions opts = new PixelMap.InitializationOptions();
        opts.size = new Size(scratchView.getWidth(), scratchView.getHeight());
        opts.pixelFormat = PixelFormat.ARGB_8888;
        PixelMapHolder bitmap = new PixelMapHolder(PixelMap.create(opts));

        Canvas canvas = new Canvas(new Texture(bitmap.getPixelMap()));

        scratchView.addDrawTask((Component.DrawTask) canvas);
        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer createBitmapFromScratchableView()");
        return bitmap;
    }

    protected void addPendingScratchPathPointsAndClear() {
        final List<ScratchPathPoint> pendingPoints;

        synchronized (pathManager) {
            if (this.pendingPathPoints.isEmpty())
                return;

            pendingPoints = new ArrayList<ScratchPathPoint>(this.pendingPathPoints);

            this.pendingPathPoints.clear();
        }

        addScratchPathPoints(pendingPoints);
    }

    @Override
    public void addScratchPathPoints(Collection<ScratchPathPoint> events) {
        final State state;
        final PixelMapHolder pathStrippedImage;
        final Canvas pathStrippedCanvas;

        synchronized (pathManager) {
            state = this.state;
            pathStrippedImage = this.pathStrippedImage;
            pathStrippedCanvas = this.pathStrippedCanvas;
        }

        switch (state) {
            case UNATTACHED:
            case CLEARED:
                break;
            case PREPARING:
                synchronized (pathManager) {
                    this.pendingPathPoints.addAll(events);
                }

                break;
            default:
                pathManager.addScratchPathPoints(events);
                pathManager.drawAndReset(pathStrippedCanvas, clearPaint);

                pathStrippedImage.uploadTexture();
        }
    }

    public void draw(Canvas canvas) {
        final State state;
        final PixelMapHolder pathStrippedImage;

        synchronized (pathManager) {
            state = this.state;
            pathStrippedImage = this.pathStrippedImage;
        }

        if (pathStrippedImage == null)
            return;

        switch (state) {
            case UNATTACHED:
            case PREPARING:
            case CLEARED:
                return;
            default:
                canvas.drawPixelMapHolder(pathStrippedImage, 0, 0, null);
        }
    }

    public void destroy() {
        synchronized (pathManager) {
            this.state = State.UNATTACHED;

            if (pathStrippedImage == null)
                return;

            pathStrippedImage.release();
            pathStrippedImage = null;

            pathStrippedCanvas = null;

            pendingPathPoints.clear();
            pathManager.clear();
        }
    }

    public void clear(boolean animationEnabled) {
        synchronized (pathManager) {
            if (animationEnabled) {
                performFadeOutClear();

                return;
            }

            hideAndMarkScratchableSurfaceViewCleared();
        }
    }

    protected void performFadeOutClear() {
        final Component view = scratchView.get();

        if (view == null)
            return;

        this.state = State.CLEARING;

        claimClearAnimation(view, System.currentTimeMillis());
        performFadeOutClear(view);
    }

    protected void claimClearAnimation(Component view, long id) {
        this.activeClearTag = id;
        Map<Integer, Object> tag = new HashMap<>();
        tag.put(1, id);
        view.setTag(tag);
    }

    protected void performFadeOutClear(Component view) {
        AnimatorProperty animatorProperty = new AnimatorProperty(view);
        animatorProperty.setDuration(clearAnimationDurationMs);
        animatorProperty.setCurveType(clearAnimationInterpolator);
        animatorProperty.scaleXFrom(1f);
        animatorProperty.scaleX(0f);
        animatorProperty.start();
    }

    @Override
    public void onStart(Animator animator) {

    }

    @Override
    public void onStop(Animator animator) {

    }

    @Override
    public void onCancel(Animator animator) {

    }

    @Override
    public void onEnd(Animator animator) {
        final Component view = scratchView.get();

        if (view == null)
            return;

        if (!activeClearTag.equals(view.getTag()))
            return;

        synchronized (pathManager) {
            if (ScratchableLayoutDrawer.this.state != State.CLEARING)
                return;

            hideAndMarkScratchableSurfaceViewCleared();
        }
    }

    @Override
    public void onPause(Animator animator) {

    }

    @Override
    public void onResume(Animator animator) {

    }

    protected void hideAndMarkScratchableSurfaceViewCleared() {
        this.state = State.CLEARED;

        Component view = scratchView.get();

        visibilityController.hide(view);
        visibilityController.showChildren(view);
    }

    private void addGlobalLayoutRequest(final Component view, final Runnable runnable) {
        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer addGlobalLayoutRequest()");
        view.setLayoutRefreshedListener(new Component.LayoutRefreshedListener() {
            @Override
            public void onRefreshed(Component component) {
                HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer addGlobalLayoutRequest() inside listener");
                if (runnable != null)
                    runnable.run();

                removeGlobalLayoutListener(view, this);
            }
        });
//                .addTreeLayoutChangedListener(new ComponentTreeObserver.GlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayoutUpdated() {
//                        HiLog.debug(ScratchoffController.LABEL, "ScratchableLayoutDrawer addGlobalLayoutRequest() inside listener");
//                        if (runnable != null)
//                            runnable.run();
//
//                        removeGlobalLayoutListener(view, this);
//                    }
//                });

        view.requestFocus();
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private void removeGlobalLayoutListener(Component view, Component.LayoutRefreshedListener listener) {

       // view.removeLa(listener);
    }

    public ScratchableLayoutDrawer setClearAnimationDurationMs(long clearAnimationDurationMs) {
        this.clearAnimationDurationMs = clearAnimationDurationMs;

        return this;
    }

    public ScratchableLayoutDrawer setClearAnimationInterpolator(int clearAnimationInterpolator) {
        this.clearAnimationInterpolator = clearAnimationInterpolator;

        return this;
    }
}
