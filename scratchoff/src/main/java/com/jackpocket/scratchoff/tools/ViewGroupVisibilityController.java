package com.jackpocket.scratchoff.tools;


import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;

public class ViewGroupVisibilityController {

    public void hide(Component view) {
        if (view == null)
            return;

        view.setVisibility(Component.HIDE);
    }

    public void hideChildren(Component view) {
        if (view == null)
            return;

        if (view instanceof ComponentContainer){
            ComponentContainer group = (ComponentContainer) view;

            for (int i = 0; i < group.getChildCount(); i++)
                group.getComponentAt(i)
                        .setVisibility(Component.HIDE);
        }
    }

    public void showChildren(Component view) {
        if (view == null)
            return;

        if (view instanceof ComponentContainer){
            ComponentContainer group = (ComponentContainer) view;

            for(int i = 0; i < group.getChildCount(); i++)
                group.getComponentAt(i)
                        .setVisibility(Component.VISIBLE);
        }
    }
}
