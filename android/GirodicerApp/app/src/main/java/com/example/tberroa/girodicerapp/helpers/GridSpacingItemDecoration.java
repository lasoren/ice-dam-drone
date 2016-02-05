package com.example.tberroa.girodicerapp.helpers;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int spacing;

    public GridSpacingItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect rect, View v, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(v);
        int column = position % spanCount;
        rect.left = spacing - column * spacing / spanCount;
        rect.right = (column + 1) * spacing / spanCount;
        if (position < spanCount) {
            rect.top = spacing;
        }
        rect.bottom = spacing;
    }
}