package android.content.pm;

import android.annotation.SuppressLint;
import android.os.Parcelable;

import java.util.List;

@SuppressLint("ParcelCreator")
public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    public ParceledListSlice(List<T> list) {
        super(list);
    }

    @Override
    public int describeContents() {
        throw new IllegalArgumentException("Stub!");
    }
}
