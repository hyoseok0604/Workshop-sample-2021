package page.chungjungsoo.to_dosample

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import page.chungjungsoo.to_dosample.todo.*
import java.util.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view - loads activity_main.xml
        setContentView(R.layout.activity_main)

        // Set app status bar color : white, force light status bar mode
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Set light status bar mode depending on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        
        if(FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build()
            )
    
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN
            )
        }else{
            init()
        }
    }
    
    private fun init() {

//        Toast.makeText(this, FirebaseAuth.getInstance().currentUser!!.uid, Toast.LENGTH_LONG).show()
        
        // Put data with custom listview adapter
        todoList.adapter = read(this)
        todoList.emptyView = helpText
    
        // Onclick listener for add button
        addBtn.setOnClickListener {
            // By pressing the add button, we will inflate an AlertDialog.
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
        
            // Get elements from custom dialog layout (add_todo_dialog.xml)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dueToAdd = dialogView.findViewById<TextView>(R.id.todoDue)
            val changeDueBtn = dialogView.findViewById<Button>(R.id.todoChangeDue)
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.todoFinishedCheckBox)
        
            // Add InputMethodManager for auto keyboard popup
            val ime = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        
            // Cursor auto focus on title when AlertDialog is inflated
            titleToAdd.requestFocus()
        
            // Show keyboard when AlertDialog is inflated
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        
            val c : Calendar = Calendar.getInstance()
        
            var year = c.get(Calendar.YEAR)
            var month = c.get(Calendar.MONTH) + 1
            var day = c.get(Calendar.DAY_OF_MONTH)
            var hour = c.get(Calendar.HOUR_OF_DAY)
            var minute = c.get(Calendar.MINUTE)
        
            dueToAdd.text = "%04d-%02d-%02d %02d:%02d".format(year, month, day, hour, minute)
        
            changeDueBtn.setOnClickListener {
                val timePickerDialog = TimePickerDialog(this,
                        { _, newHour, newMinute ->
                            hour = newHour
                            minute = newMinute
                        
                            dueToAdd.text = "%04d-%02d-%02d %02d:%02d".format(year, month, day, hour, minute)
                        }, hour, minute, true
                )
            
                val datePickerDialog = DatePickerDialog(this,
                        { _, newYear, newMonth, newDay ->
                            year = newYear
                            month = newMonth
                            day = newDay
                        
                            timePickerDialog.show()
                        }, year, month - 1, day)
            
                datePickerDialog.show()
            }
        
            // Add positive button and negative button for AlertDialog.
            // Pressing the positive button: Add data to the database and also add them in listview and update.
            // Pressing the negative button: Do nothing. Close the AlertDialog
            val add = builder.setView(dialogView)
                    .setPositiveButton("추가") { _, _ ->
                        if (!TextUtils.isEmpty(titleToAdd.text.trim())) {
                            val tmpAdapter = todoList.adapter as TodoListViewAdapter
                            // Add item to the database
                            val todo = Todo(
                                    if(tmpAdapter.count == 0) 0
                                    else tmpAdapter.getItem(tmpAdapter.count - 1)!!.uid!! + 1,
                                    titleToAdd.text.toString(),
                                    descriptionToAdd.text.toString(),
                                    dueToAdd.text.toString(),
                                    finishedToAdd.isChecked
                            )

                            write(todo)
                        
                            // Add them to listview and update.
                            (todoList.adapter as TodoListViewAdapter).add(todo)
                            (todoList.adapter as TodoListViewAdapter).notifyDataSetChanged()
                        
                            // Close keyboard
                            ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                        }
                        else {
                            Toast.makeText(this,
                                    "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("취소") {_, _ ->
                        // Cancel Btn. Do nothing. Close keyboard.
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    .show()
                    .getButton(DialogInterface.BUTTON_POSITIVE)
        
            // Default status of add button should be disabled. Because when AlertDialog inflates,
            // the title is empty by default and we do not want empty titles to be added to listview
            // and in databases.
            add.isEnabled = false
        
            // Listener for title text. If something is inputted in title, we should re-enable the add button.
            titleToAdd.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (!TextUtils.isEmpty(p0.toString().trim())) {
                        add.isEnabled = true
                    }
                    else {
                        titleToAdd.error = "TODO 제목을 입력하세요!"
                        add.isEnabled = false
                    }
                }
            
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if(requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            
            if(resultCode == Activity.RESULT_OK) {
                init()
            }
        }
    }
    
    companion object {
        private const val RC_SIGN_IN = 123
    }
}