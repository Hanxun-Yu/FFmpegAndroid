package com.ycuwq.datepicker.CommonPicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ycuwq.datepicker.R;
import com.ycuwq.datepicker.WheelPicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhanxun
 * 2018/8/17
 * description:
 */
public abstract class ParentPicker extends LinearLayout {
    protected List<WheelPicker> mSubPickers = new ArrayList<>();
    private OnSelectedListener mOnSelectedListener;

    public ParentPicker(Context context) {
        this(context, null);
    }

    public ParentPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParentPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.parseColor("#f7f7f7"));
        setOrientation(LinearLayout.HORIZONTAL);
        addSubPicker();
        initAttrs(context, attrs);
    }

    protected abstract void addSubPicker();

    private void initAttrs(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker);
        int textSize = a.getDimensionPixelSize(R.styleable.DatePicker_itemTextSize,
                getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize));
        int textColor = a.getColor(R.styleable.DatePicker_itemTextColor,
                Color.BLACK);
        boolean isTextGradual = a.getBoolean(R.styleable.DatePicker_textGradual, true);
        boolean isCyclic = a.getBoolean(R.styleable.DatePicker_wheelCyclic, false);
        int halfVisibleItemCount = a.getInteger(R.styleable.DatePicker_halfVisibleItemCount, 2);
        int selectedItemTextColor = a.getColor(R.styleable.DatePicker_selectedTextColor,
                getResources().getColor(R.color.com_ycuwq_datepicker_selectedTextColor));
        int selectedItemTextSize = a.getDimensionPixelSize(R.styleable.DatePicker_selectedTextSize,
                getResources().getDimensionPixelSize(R.dimen.WheelSelectedItemTextSize));
        int itemWidthSpace = a.getDimensionPixelSize(R.styleable.DatePicker_itemWidthSpace,
                getResources().getDimensionPixelOffset(R.dimen.WheelItemWidthSpace));
        int itemHeightSpace = a.getDimensionPixelSize(R.styleable.DatePicker_itemHeightSpace,
                getResources().getDimensionPixelOffset(R.dimen.WheelItemHeightSpace));
        boolean isZoomInSelectedItem = a.getBoolean(R.styleable.DatePicker_zoomInSelectedItem, true);
        boolean isShowCurtain = a.getBoolean(R.styleable.DatePicker_wheelCurtain, true);
        int curtainColor = a.getColor(R.styleable.DatePicker_wheelCurtainColor, Color.WHITE);
        boolean isShowCurtainBorder = a.getBoolean(R.styleable.DatePicker_wheelCurtainBorder, true);
        int curtainBorderColor = a.getColor(R.styleable.DatePicker_wheelCurtainBorderColor,
                getResources().getColor(R.color.com_ycuwq_datepicker_divider));
        a.recycle();

        setBackgroundDrawable(getBackground());
        setTextSize(textSize);
        setTextColor(textColor);
        setTextGradual(isTextGradual);
        setCyclic(isCyclic);
        setHalfVisibleItemCount(halfVisibleItemCount);
        setSelectedItemTextColor(selectedItemTextColor);
        setSelectedItemTextSize(selectedItemTextSize);
        setItemWidthSpace(itemWidthSpace);
        setItemHeightSpace(itemHeightSpace);
        setZoomInSelectedItem(isZoomInSelectedItem);
        setShowCurtain(isShowCurtain);
        setCurtainColor(curtainColor);
        setShowCurtainBorder(isShowCurtainBorder);
        setCurtainBorderColor(curtainBorderColor);
        setSubOnSelectChanged();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setBackgroundColor(color);
            }
        }
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setBackgroundResource(resid);
            }
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setBackgroundDrawable(background);
            }
        }
    }

    /**
     * 一般列表的文本颜色
     *
     * @param textColor 文本颜色
     */
    public void setTextColor(@ColorInt int textColor) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setTextColor(textColor);
            }
        }
    }

    /**
     * 一般列表的文本大小
     *
     * @param textSize 文字大小
     */
    public void setTextSize(int textSize) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setTextSize(textSize);
            }
        }
    }

    /**
     * 设置被选中时候的文本颜色
     *
     * @param selectedItemTextColor 文本颜色
     */
    public void setSelectedItemTextColor(@ColorInt int selectedItemTextColor) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setSelectedItemTextColor(selectedItemTextColor);
            }
        }
    }

    /**
     * 设置被选中时候的文本大小
     *
     * @param selectedItemTextSize 文字大小
     */
    public void setSelectedItemTextSize(int selectedItemTextSize) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setSelectedItemTextSize(selectedItemTextSize);
            }
        }
    }


    /**
     * 设置显示数据量的个数的一半。
     * 为保证总显示个数为奇数,这里将总数拆分，itemCount = mHalfVisibleItemCount * 2 + 1
     *
     * @param halfVisibleItemCount 总数量的一半
     */
    public void setHalfVisibleItemCount(int halfVisibleItemCount) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setHalfVisibleItemCount(halfVisibleItemCount);
            }
        }
    }

    /**
     * Sets item width space.
     *
     * @param itemWidthSpace the item width space
     */
    public void setItemWidthSpace(int itemWidthSpace) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setItemWidthSpace(itemWidthSpace);
            }
        }
    }

    /**
     * 设置两个Item之间的间隔
     *
     * @param itemHeightSpace 间隔值
     */
    public void setItemHeightSpace(int itemHeightSpace) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setItemHeightSpace(itemHeightSpace);
            }
        }
    }


    /**
     * Set zoom in center item.
     *
     * @param zoomInSelectedItem the zoom in center item
     */
    public void setZoomInSelectedItem(boolean zoomInSelectedItem) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setZoomInSelectedItem(zoomInSelectedItem);
            }
        }
    }

    /**
     * 设置是否循环滚动。
     * set wheel cyclic
     *
     * @param cyclic 上下边界是否相邻
     */
    public void setCyclic(boolean cyclic) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setCyclic(cyclic);
            }
        }
    }

    /**
     * 设置文字渐变，离中心越远越淡。
     * Set the text color gradient
     *
     * @param textGradual 是否渐变
     */
    public void setTextGradual(boolean textGradual) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setTextGradual(textGradual);
            }
        }
    }


    /**
     * 设置中心Item是否有幕布遮盖
     * set the center item curtain cover
     *
     * @param showCurtain 是否有幕布
     */
    public void setShowCurtain(boolean showCurtain) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setShowCurtain(showCurtain);
            }
        }
    }

    /**
     * 设置幕布颜色
     * set curtain color
     *
     * @param curtainColor 幕布颜色
     */
    public void setCurtainColor(@ColorInt int curtainColor) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setCurtainColor(curtainColor);
            }
        }
    }

    /**
     * 设置幕布是否显示边框
     * set curtain border
     *
     * @param showCurtainBorder 是否有幕布边框
     */
    public void setShowCurtainBorder(boolean showCurtainBorder) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setShowCurtainBorder(showCurtainBorder);
            }
        }
    }

    /**
     * 幕布边框的颜色
     * curtain border color
     *
     * @param curtainBorderColor 幕布边框颜色
     */
    public void setCurtainBorderColor(@ColorInt int curtainBorderColor) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setCurtainBorderColor(curtainBorderColor);
            }
        }
    }


    /**
     * 设置指示器文字的颜色
     * set indicator text color
     *
     * @param textColor 文本颜色
     */
    public void setIndicatorTextColor(@ColorInt int textColor) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setIndicatorTextColor(textColor);
            }
        }
    }

    /**
     * 设置指示器文字的大小
     * indicator text size
     *
     * @param textSize 文本大小
     */
    public void setIndicatorTextSize(int textSize) {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setTextSize(textSize);
            }
        }
    }

    public void setSubOnSelectChanged() {
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            for (WheelPicker item : mSubPickers) {
                if (item != null)
                    item.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
                        @Override
                        public void onWheelSelected(Object item, int position) {
                            onSelectChanged();
                        }
                    });
            }
        }
    }

    public static class PickerData<T> {
        public PickerData(List<T> data, String unit) {
            this.data = data;
            this.unit = unit;
        }

        public List<T> data;
        public String unit;
    }

    private void onSelectChanged() {
        Object[] data;
        if (mSubPickers != null && !mSubPickers.isEmpty()) {
            data = new String[mSubPickers.size()];
            for (int i = 0; i < mSubPickers.size(); i++) {
                data[i] = mSubPickers.get(i).getCurrentSelectItem();
            }
            if (mOnSelectedListener != null)
                mOnSelectedListener.onSelected(data);
        }
    }

    /**
     * Sets on date selected listener.
     *
     * @param onDateSelectedListener the on date selected listener
     */
    public void setOnSelectedListener(OnSelectedListener onDateSelectedListener) {
        mOnSelectedListener = onDateSelectedListener;
    }

    /**
     * The interface On date selected listener.
     */
    public interface OnSelectedListener {
        void onSelected(Object... data);
    }

    private void onSelected(Object... data) {
        if (mOnSelectedListener != null)
            mOnSelectedListener.onSelected(data);
    }
}
