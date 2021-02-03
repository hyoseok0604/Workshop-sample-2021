package page.chungjungsoo.to_dosample.todo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.R


class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )
        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDescription)
        val due : TextView = view.findViewById(R.id.listDue)
        val isFinished : TextView = view.findViewById(R.id.listIsFinished)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)

        db = TodoDatabaseHelper(this.context)

        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.description
        due.text = todo.due!!.split(" ")[0] // YYYY-MM-DD HH:SS
        isFinished.text = context.resources.getString( if(todo.finished) R.string.finished else R.string.not_finished )

        // OnClick Listener for edit button on every ListView items
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dueToAdd = dialogView.findViewById<TextView>(R.id.todoDue)
            val changeDueBtn = dialogView.findViewById<Button>(R.id.todoChangeDue)
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.todoFinishedCheckBox)
            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            titleToAdd.setText(todo.title)
            descriptionToAdd.setText(todo.description)
            dueToAdd.text = todo.due
            finishedToAdd.isChecked = todo.finished

            var year = todo.due!!.substring(0, 4).toInt()
            var month = todo.due!!.substring(5, 7).toInt()
            var day = todo.due!!.substring(8, 10).toInt()
            var hour = todo.due!!.substring(11, 13).toInt()
            var minute = todo.due!!.substring(14, 16).toInt()

            changeDueBtn.setOnClickListener {
                val timePickerDialog = TimePickerDialog(context,
                    { _, newHour, newMinute ->
                        hour = newHour
                        minute = newMinute

                        dueToAdd.text = "%04d-%02d-%02d %02d:%02d".format(year, month, day, hour, minute)
                    }, hour, minute, true
                )

                val datePickerDialog = DatePickerDialog(context,
                    { _, newYear, newMonth, newDay ->
                        year = newYear
                        month = newMonth + 1
                        day = newDay

                        timePickerDialog.show()
                    }, year, month - 1, day)

                datePickerDialog.show()
            }

            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    val tmp = Todo(
                        items[position].uid,
                        titleToAdd.text.toString(),
                        descriptionToAdd.text.toString(),
                        dueToAdd.text.toString(),
                        finishedToAdd.isChecked
                    )

//                    val result = db.updateTodo(tmp, position)
//                    if (result) {
//                        todo.title = titleToAdd.text.toString()
//                        todo.description = descriptionToAdd.text.toString()
//                        todo.due = dueToAdd.text.toString()
//                        todo.finished = finishedToAdd.isChecked
//
//                        notifyDataSetChanged()
//                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
//                    }
//                    else {
//                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
//                        notifyDataSetChanged()
//                    }
                    update(context, position, tmp)
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
//            val result = db.delTodo(position)
//            if (result) {
//                items.removeAt(position)
//                notifyDataSetChanged()
//            }
//            else {
//                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
//                notifyDataSetChanged()
//            }
            delete(context, position, items[position].uid!!)
        }

        return view
    }
}