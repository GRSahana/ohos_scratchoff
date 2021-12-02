package com.jackpocket.scratchoff.views;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.DependentLayout;
import ohos.agp.render.Canvas;
import ohos.app.Context;
import ohos.utils.Sequenceable;

import com.jackpocket.scratchoff.ScratchoffController;

public class ScratchableRelativeLayout extends DependentLayout implements ScratchableLayout, Component.DrawTask {

    private final ScratchoffController controller;

    public ScratchableRelativeLayout(Context context) {
        super(context);

        this.controller = createScratchoffController();
    }

    public ScratchableRelativeLayout(Context context, AttrSet attrs) {
        super(context, attrs);

        this.controller = createScratchoffController();
    }


    public ScratchableRelativeLayout(Context context, AttrSet attrs, String defStyle) {
        super(context, attrs, defStyle);

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
//    protected void onRestoreAbilityState(Sequenceable state) {
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