package com.jackpocket.scratchoff.test.slice;

import com.jackpocket.scratchoff.ScratchoffController;
import com.jackpocket.scratchoff.ScratchoffThresholdProcessor;
import com.jackpocket.scratchoff.test.ResourceTable;
import com.jackpocket.scratchoff.views.ScratchableLinearLayout;

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.animation.Animator;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MainAbilitySlice extends AbilitySlice
        implements ScratchoffController.ThresholdChangedListener, Component.TouchEventListener{

    private ScratchoffController controller;
    private WeakReference<Text> scratchPercentTitleView = null;
    private ComponentContainer scratchViewBehind;

    public static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0x00201, "ScratchOff Library");


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        HiLog.debug(MainAbilitySlice.LABEL, "onStart() ");

        this.scratchPercentTitleView = new WeakReference(findComponentById(ResourceTable.Id_scratch_value_title));

        scratchViewBehind = (ComponentContainer) findComponentById(ResourceTable.Id_scratch_view);
        this.controller = ScratchoffController.findByViewId(this, ResourceTable.Id_scratch_view)
                .setThresholdChangedListener(this)
                .setTouchRadiusDip(this, 25)
                .setThresholdCompletionPercent(.4f)
                .setClearAnimationEnabled(true)
                .setClearAnimationDuration(1, TimeUnit.SECONDS)
                .setClearAnimationInterpolator(Animator.CurveType.LINEAR)
                .setTouchRadiusPx(25)
                .setThresholdAccuracyQuality(ScratchoffThresholdProcessor.Quality.LOW)
                .setMatchLayoutWithBehindView((ComponentContainer) findComponentById(ResourceTable.Id_scratch_view_behind))
                .setStateRestorationEnabled(false)
                .attach();
        Button scratchButton = (Button) findComponentById(ResourceTable.Id_scratch_button);
        scratchButton.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                resetActionClicked(component);
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    public void onScratchPercentChanged(ScratchoffController controller, float percentCompleted) {
        scratchPercentTitleView.get().setText("Scratched "+percentCompleted*100+"%");
    }

    @Override
    public void onScratchThresholdReached(ScratchoffController controller) {
        scratchViewBehind.setVisibility(Component.HIDE);

    }

    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        switch (touchEvent.getAction()) {
//            case TouchEvent. -> Log.d(TAG, "Observed ACTION_DOWN")
//            MotionEvent.ACTION_UP -> Log.d(TAG, "Observed ACTION_UP")
        }
        HiLog.debug(MainAbilitySlice.LABEL, "TouchEvent "+touchEvent.getAction());


        // Our return is ignored here

        return false;
    }

    public void resetActionClicked(Component component) {
        // Reset the scratchable View's background color, as the ScratchableLayoutDrawer
        // will set the background to Color.TRANSPARENT after capturing the content
        ScratchableLinearLayout scratchableLinearLayout = (ScratchableLinearLayout) findComponentById(ResourceTable.Id_scratch_view);
        //scratchableLinearLayout.setBackground();
        ShapeElement backgroundShape = new ShapeElement();
        backgroundShape.setShape(ShapeElement.RECTANGLE);
        backgroundShape.setRgbColor(RgbColor.fromArgbInt(Color.TRANSPARENT.getValue()));
        scratchableLinearLayout.setBackground(backgroundShape);
        controller.attach();
    }

}
