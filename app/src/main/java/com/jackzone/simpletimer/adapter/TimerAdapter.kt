package com.jackzone.simpletimer.adapter

import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jackzone.simpletimer.BaseActivity
import com.jackzone.simpletimer.R
import com.jackzone.simpletimer.extension.*
import com.jackzone.simpletimer.interfaces.MyActionModeCallback
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.model.TimerEvent
import com.jackzone.simpletimer.model.TimerState
import com.jackzone.simpletimer.view.MyRecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_timer.view.*
import org.greenrobot.eventbus.EventBus
import kotlin.math.max
import kotlin.math.min

class TimerAdapter(
    val activity: BaseActivity,
    private val recyclerView: MyRecyclerView,
    private val onItemClick: (Timer) -> Unit,
) : ListAdapter<Timer, TimerAdapter.ViewHolder>(diffUtil), Animation.AnimationListener {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(item: Timer, callback: (itemView: View, adapterPosition: Int) -> Unit): View {
            return itemView.apply {
                callback(this, adapterPosition)

                setOnClickListener { viewClicked(item) }
                setOnLongClickListener { viewLongClicked(); true }
            }
        }

        private fun viewClicked(any: Timer) {
            if (actModeCallback.isSelectable) {
                val currentPosition = adapterPosition - positionOffset
                val isSelected = selectedKeys.contains(getItemSelectionKey(currentPosition))
                toggleItemSelection(!isSelected, currentPosition, true)
            } else {
                onItemClick.invoke(any)
            }
            lastLongPressedItem = -1
        }

        private fun viewLongClicked() {
            val currentPosition = adapterPosition - positionOffset
            if (!actModeCallback.isSelectable) {
                activity.startActionMode(actModeCallback, ActionMode.TYPE_FLOATING)
            }

            toggleItemSelection(true, currentPosition, true)
            itemLongClicked(currentPosition)
        }
    }

    private val layoutInflater = activity.layoutInflater
    private var actModeCallback: MyActionModeCallback
    private var selectedKeys = LinkedHashSet<Int>()
    private var positionOffset = 0
    private var actMode: ActionMode? = null
    private var lastLongPressedItem = -1

    private fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> deleteItems()
        }
    }

    private fun getItemSelectionKey(position: Int) = getItem(position).id

    private fun getItemKeyPosition(key: Int): Int {
        var position = -1
        for (i in 0 until itemCount) {
            if (key == getItem(i).id) {
                position = i
                break
            }
        }
        return position
    }

    private fun toggleDeleteMenu(visible: Boolean) {
        activity.apply {
            delete_menu.beVisibleIf(visible)
            setting.beVisibleIf(!visible)
        }
    }

    private fun onActionModeCreated() {
        toggleDeleteMenu(true)
        activity.let {
            it.collapsing_toolbar.setCollapsedTitleTextColor(it.getColor(android.R.color.transparent))
            val inAnim = AnimationUtils.loadAnimation(it, R.anim.anim_bottom_button_in)
            inAnim.setAnimationListener(this)
            it.delete_btn.startAnimation(inAnim)
        }
    }

    private fun onActionModeDestroyed() {
        toggleDeleteMenu(false)
        activity.let {
            it.collapsing_toolbar.setCollapsedTitleTextColor(it.getColor(R.color.color_text))
            val outAnim = AnimationUtils.loadAnimation(it, R.anim.anim_bottom_button_out)
            outAnim.setAnimationListener(this)
            it.delete_btn.startAnimation(outAnim)
        }
    }

    init {
        activity.select_all_btn.setOnClickListener {
            if (itemCount == selectedKeys.size) {
                finishActMode()
            } else {
                selectAll()
            }
        }
        activity.cancel_btn.setOnClickListener {
            finishActMode()
        }
        activity.delete_btn.setOnClickListener {
            if (selectedKeys.isNotEmpty()) deleteItems()
        }
        actModeCallback = object : MyActionModeCallback() {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                actionItemPressed(item.itemId)
                return true
            }

            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                isSelectable = true
                actMode = actionMode
                onActionModeCreated()
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                isSelectable = false
                (selectedKeys.clone() as HashSet<Int>).forEach {
                    val position = getItemKeyPosition(it)
                    if (position != -1) {
                        toggleItemSelection(false, position, false)
                    }
                }
                updateTitle()
                selectedKeys.clear()
                activity.delete_title?.text = ""
                actMode = null
                lastLongPressedItem = -1
                onActionModeDestroyed()
            }
        }
        setupDragListener(true)
    }

    private fun toggleItemSelection(select: Boolean, pos: Int, updateTitle: Boolean = true) {
        val itemKey = getItemSelectionKey(pos) ?: return
        if ((select && selectedKeys.contains(itemKey)) || (!select && !selectedKeys.contains(itemKey))) {
            return
        }

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos + positionOffset)

        if (updateTitle) {
            updateTitle()
        }

        if (selectedKeys.isEmpty()) {
            finishActMode()
        }
    }

    private fun updateTitle() {
        val selectableItemCount = itemCount
        val selectedCount = min(selectedKeys.size, selectableItemCount)
        val deleteTitle = activity.delete_title
        val oldTitle = deleteTitle.text
        val newTitle = "$selectedCount / $selectableItemCount"
        if (oldTitle != newTitle) {
            deleteTitle.text = newTitle
            actMode?.invalidate()
        }
    }

    private fun itemLongClicked(position: Int) {
        recyclerView.setDragSelectActive(position)
        lastLongPressedItem = if (lastLongPressedItem == -1) {
            position
        } else {
            val min = min(lastLongPressedItem, position)
            val max = max(lastLongPressedItem, position)
            for (i in min..max) {
                toggleItemSelection(true, i, false)
            }
            updateTitle()
            position
        }
    }

    private fun getSelectedItemPositions(sortDescending: Boolean = true): ArrayList<Int> {
        val positions = ArrayList<Int>()
        val keys = selectedKeys.toList()
        keys.forEach {
            val position = getItemKeyPosition(it)
            if (position != -1) {
                positions.add(position)
            }
        }

        if (sortDescending) {
            positions.sortDescending()
        }
        return positions
    }

    private fun selectAll() {
        val cnt = itemCount - positionOffset
        for (i in 0 until cnt) {
            toggleItemSelection(true, i, false)
        }
        lastLongPressedItem = -1
        updateTitle()
    }

    private fun setupDragListener(enable: Boolean) {
        if (enable) {
            recyclerView.setupDragListener(object : MyRecyclerView.MyDragListener {
                override fun selectItem(position: Int) {
                    toggleItemSelection(true, position, true)
                }

                override fun selectRange(initialSelection: Int, lastDraggedIndex: Int, minReached: Int, maxReached: Int) {
                    selectItemRange(
                        initialSelection,
                        max(0, lastDraggedIndex - positionOffset),
                        max(0, minReached - positionOffset),
                        maxReached - positionOffset
                    )
                    if (minReached != maxReached) {
                        lastLongPressedItem = -1
                    }
                }
            })
        } else {
            recyclerView.setupDragListener(null)
        }
    }

    private fun selectItemRange(from: Int, to: Int, min: Int, max: Int) {
        if (from == to) {
            (min..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            return
        }

        if (to < from) {
            for (i in to..from) {
                toggleItemSelection(true, i, true)
            }

            if (min > -1 && min < to) {
                (min until to).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            }

            if (max > -1) {
                for (i in from + 1..max) {
                    toggleItemSelection(false, i, true)
                }
            }
        } else {
            for (i in from..to) {
                toggleItemSelection(true, i, true)
            }

            if (max > -1 && max > to) {
                (to + 1..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            }

            if (min > -1) {
                for (i in min until from) {
                    toggleItemSelection(false, i, true)
                }
            }
        }
    }

    private fun finishActMode() {
        actMode?.finish()
    }

    private fun removeSelectedItems(positions: ArrayList<Int>) {
        positions.forEach {
            notifyItemRemoved(it)
        }
        finishActMode()
    }

    private fun deleteItems() {
        val positions = getSelectedItemPositions()
        val timersToRemove = positions.map { position ->
            getItem(position)
        }
        removeSelectedItems(positions)
        timersToRemove.forEach(::deleteTimer)
    }

    private fun deleteTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Delete(timer.id!!))
        activity.hideTimerNotification(timer.id!!)
    }

    private fun setupView(view: View, timer: Timer) {
        view.apply {
            val isSelected = selectedKeys.contains(timer.id)
            timer_frame.isSelected = isSelected

            timer_label.text = timer.label

            timer_time.text = when (timer.state) {
                is TimerState.Finished -> 0.getFormattedDuration()
                is TimerState.Idle -> timer.seconds.getFormattedDuration()
                is TimerState.Paused -> timer.state.tick.getFormattedDuration()
                is TimerState.Running -> timer.state.tick.getFormattedDuration()
            }

            timer_reset.setOnClickListener {
                resetTimer(timer)
            }

            timer_play_pause.setOnClickListener {
                activity.handleNotificationPermission {
                    if (it) {
                        when (val state = timer.state) {
                            is TimerState.Idle -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                            is TimerState.Paused -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, state.tick))
                            is TimerState.Running -> EventBus.getDefault().post(TimerEvent.Pause(timer.id!!, state.tick))
                            is TimerState.Finished -> EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                        }
                    } else {
                        activity.toast(R.string.no_post_notifications_permissions)
                    }
                }
            }

            val state = timer.state
            val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
            timer_reset.apply {
                visibility = if (!resetPossible) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }
            val drawableId = if (state is TimerState.Running) R.drawable.ic_pause_vector else R.drawable.ic_play_vector
            timer_play_pause.setImageDrawable(activity.resources.getDrawable(drawableId))
        }
    }

    private fun resetTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Reset(timer.id!!))
        activity.hideTimerNotification(timer.id!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_timer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(getItem(position)) { itemView, _ ->
            setupView(itemView, getItem(position))
        }
        holder.itemView.tag = holder
    }

    override fun onAnimationStart(animation: Animation) {
        if (actModeCallback.isSelectable) {
            activity.apply {
                add_btn.beVisibleIf(false)
                delete_btn.beVisibleIf(true)
            }
        }
    }

    override fun onAnimationEnd(animation: Animation?) {
        if (!actModeCallback.isSelectable) {
            activity.apply {
                add_btn.beVisibleIf(true)
                delete_btn.beVisibleIf(false)
            }
        }
    }

    override fun onAnimationRepeat(animation: Animation) { }
}