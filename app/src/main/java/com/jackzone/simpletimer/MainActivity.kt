package com.jackzone.simpletimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.jackzone.simpletimer.dialog.EditTimerDialog
import com.jackzone.simpletimer.extension.config
import com.jackzone.simpletimer.helper.Config
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.model.TimerState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tool_bar)
        addBtn.setOnClickListener {
            EditTimerDialog(this, Timer(
                null,
                config.timerSeconds,
                TimerState.Idle,
                config.timerVibrate,
                config.timerSoundUri,
                config.timerSoundTitle,
                config.timerLabel ?: "",
                System.currentTimeMillis(),
                config.timerChannelId
            )) {
                refreshTimers()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            //TODO
            R.id.setting -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun refreshTimers(scrollToLatest: Boolean = false) {
//        activity?.timerHelper?.getTimers { timers ->
//            activity?.runOnUiThread {
//                timerAdapter.submitList(timers) {
//                    getView()?.post {
//                        if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
//                            view.timers_list.scrollToPosition(timerPositionToScrollTo)
//                            timerPositionToScrollTo = INVALID_POSITION
//                        } else if (scrollToLatest) {
//                            view.timers_list.scrollToPosition(timers.lastIndex)
//                        }
//                    }
//                }
//            }
//        }
    }
}