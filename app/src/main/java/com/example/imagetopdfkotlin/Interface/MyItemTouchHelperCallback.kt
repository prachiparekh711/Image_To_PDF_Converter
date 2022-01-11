package com.example.imagetopdfkotlin.Interface

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class MyItemTouchHelperCallback(  // interface
    var callbackItemTouch: CallbackItemTouch
) :
    ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false // swiped disabled
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // movements drag
        return makeFlag(
            ItemTouchHelper.ACTION_STATE_DRAG,
            dragFlags
        ) // as parameter, action drag and flags drag
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        callbackItemTouch.itemTouchOnMove(
            viewHolder.adapterPosition,
            target.adapterPosition
        ) // information to the interface
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // swiped disabled
    }
}
