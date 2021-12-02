package com.jackpocket.scratchoff;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

import com.jackpocket.scratchoff.paths.ScratchPathPoint;

import java.util.List;

public class ScratchoffState implements Sequenceable {

    private final int[] size;
    private final boolean thresholdReached;

    private final List<ScratchPathPoint> events;

    public ScratchoffState(
            Sequenceable state,
            int[] size,
            boolean thresholdReached,
            List<ScratchPathPoint> events) {

        this.size = size;
        this.thresholdReached = thresholdReached;
        this.events = events;
    }

    public ScratchoffState(Parcel in) {
        this.size = new int[]{in.readInt(), in.readInt()};
        this.thresholdReached = in.readInt() == 1;
        this.events = (List<ScratchPathPoint>) in.readList();
    }

    public int[] getLayoutSize() {
        return size;
    }

    public boolean isThresholdReached() {
        return thresholdReached;
    }

    public List<ScratchPathPoint> getPathHistory() {
        return events;
    }

    public static final Sequenceable.Producer<ScratchoffState> CREATOR = new Sequenceable.Producer<ScratchoffState>() {

        @Override
        public ScratchoffState createFromParcel(Parcel in) {
            return new ScratchoffState(in);
        }

//        @Override
//        public ScratchoffState[] newArray(int size) {
//            return new ScratchoffState[size];
//        }
    };

    @Override
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(size[0]);
        parcel.writeInt(size[1]);
        parcel.writeInt(thresholdReached ? 1 : 0);
        parcel.writeList(events);
        return false;
    }

    @Override
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }
}
