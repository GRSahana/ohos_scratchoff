package com.jackpocket.scratchoff.paths;


import ohos.agp.components.DragEvent;
import ohos.multimodalinput.event.ManipulationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

import java.util.ArrayList;
import java.util.List;

public class ScratchPathPoint implements Sequenceable {

    public final int pointerIndex;
    public final float x;
    public final float y;
    public final int action;

    public ScratchPathPoint(
            int pointerIndex,
            float x,
            float y,
            int action) {

        this.pointerIndex = pointerIndex;
        this.x = x;
        this.y = y;
        this.action = action;
    }

    protected ScratchPathPoint(Parcel in) {
        this.pointerIndex = in.readInt();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.action = in.readInt();
    }

    @Override
    public boolean hasFileDescriptor() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ScratchPathPoint))
            return false;

        ScratchPathPoint another = (ScratchPathPoint) obj;

        return another.x == this.x
                && another.y == this.y
                && another.action == this.action;
    }

    //DragEvent for some function, and Manipulation event for others, so difficult to change now
    public static List<ScratchPathPoint> create(TouchEvent event) {
        final int historySize = event.getPhase();
        final int pointersCount = event.getPointerCount();

        ArrayList<ScratchPathPoint> events = new ArrayList<ScratchPathPoint>((historySize * pointersCount) + pointersCount);

        for (int historyIndex = 0; historyIndex < historySize; historyIndex++) {
            for (int pointerIndex = 0; pointerIndex < pointersCount; pointerIndex++) {
                events.add(
                        new ScratchPathPoint(
                                pointerIndex,
                                event.getPointerScreenPosition(historyIndex).getX(),
                                event.getPointerScreenPosition(historyIndex).getY(),
                                TouchEvent.POINT_MOVE));
            }
        }

        for (int pointerIndex = 0; pointerIndex < pointersCount; pointerIndex++) {
            events.add(
                    new ScratchPathPoint(
                            pointerIndex,
                            event.getPointerPosition(pointerIndex).getX(),
                            event.getPointerPosition(pointerIndex).getY(),
                            event.getAction()));
        }

        return events;
    }

    public static final Producer<ScratchPathPoint> CREATOR = new Producer<ScratchPathPoint>() {

        @Override
        public ScratchPathPoint createFromParcel(Parcel in) {
            return new ScratchPathPoint(in);
        }


//        @Override
//        public ScratchPathPoint[] newArray(int size) {
//            return new ScratchPathPoint[size];
//        }
    };

    @Override
    public boolean marshalling(Parcel dest) {
        dest.writeInt(pointerIndex);
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeInt(action);
        return true;
    }

    @Override
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }
}
