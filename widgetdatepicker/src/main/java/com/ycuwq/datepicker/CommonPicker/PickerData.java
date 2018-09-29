package com.ycuwq.datepicker.CommonPicker;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PickerData implements Parcelable {
    public PickerData(List<String> data, String unit,int selectedIndex) {
        this.data = data;
        this.unit = unit;
        this.selectedIndex = selectedIndex;
    }

    public List<String> data;
    public String unit;
    public int selectedIndex;

    protected PickerData(Parcel in) {
        data = in.createStringArrayList();
        unit = in.readString();
        selectedIndex = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(data);
        dest.writeString(unit);
        dest.writeInt(selectedIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PickerData> CREATOR = new Creator<PickerData>() {
        @Override
        public PickerData createFromParcel(Parcel in) {
            return new PickerData(in);
        }

        @Override
        public PickerData[] newArray(int size) {
            return new PickerData[size];
        }
    };
}