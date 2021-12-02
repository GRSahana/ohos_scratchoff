package com.jackpocket.scratchoff.views;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.render.Canvas;
import ohos.app.Context;
import ohos.utils.Sequenceable;

import com.jackpocket.scratchoff.ScratchoffController;

public class ScratchableLinearLayout extends DirectionalLayout implements ScratchableLayout, Component.DrawTask {

    private final ScratchoffController controller;

    public ScratchableLinearLayout(Context context) {
        super(context);

        this.controller = createScratchoffController();
    }

    public ScratchableLinearLayout(Context context, AttrSet attrs) {
        super(context, attrs);

        this.controller = createScratchoffController();
    }

    public ScratchableLinearLayout(Context context, AttrSet attrs, String styleName) {
        super(context, attrs, styleName);

        this.controller = createScratchoffController();
    }

    protected ScratchoffController createScratchoffController() {
        return new ScratchoffController(this);
    }

//    @Override
//    protected Sequenceable onSaveInstanceState() {
//        return controller.parcelize(super.onSaveInstanceState());
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Sequenceable state) {
//        controller.setStateRestorationParcel(state);
//
//        super.onRestoreInstanceState(state);
//    }

    @Override
    public ScratchoffController getScratchoffController() {
        return controller;
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        controller.draw(canvas);
    }
}