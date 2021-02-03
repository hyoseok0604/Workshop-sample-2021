package page.chungjungsoo.to_dosample.todo

import android.content.Context
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import page.chungjungsoo.to_dosample.R

/**
 * Created by 장효석 on 2021-02-03.
 */

private var database : DatabaseReference = Firebase.database.reference
private val UID : String = FirebaseAuth.getInstance().currentUser!!.uid
private var adapter : TodoListViewAdapter? = null

fun write(data: Todo){
    database.child("users").child(UID).child("${data.uid}").setValue(data)
        .addOnSuccessListener { Log.d("JHSApps", "Success") }
        .addOnFailureListener { Log.d("JHSApps", "Failure") }
}

fun update(context: Context, position: Int, data: Todo){
    database.child("users").child(UID).child("${data.uid}").setValue(data)
        .addOnSuccessListener {
            Toast.makeText(context, "수정 성공", Toast.LENGTH_SHORT).show()
            adapter!!.items[position] = data
            adapter!!.notifyDataSetChanged()
        }
        .addOnFailureListener {
            Toast.makeText(context, "수정 실패", Toast.LENGTH_SHORT).show()
            adapter!!.notifyDataSetChanged()
        }
}

fun delete(context: Context, index: Int, uid: Int){
    database.child("users").child(UID).child("$uid").removeValue()
        .addOnSuccessListener {
            Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
            adapter!!.items.removeAt(index)
            adapter!!.notifyDataSetChanged()
        }
        .addOnFailureListener {
            Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
            adapter!!.notifyDataSetChanged()
        }
}

fun read(context: Context): TodoListViewAdapter{
    val data = arrayListOf<Todo>()

    val adpt = TodoListViewAdapter(context, R.layout.todo_item, data)

    adapter = adpt

    database.child("users").child(UID).get().addOnSuccessListener { d1 ->
        d1.children.forEach { d2 ->
            Log.d("JHSApps", d2.key!!)
            data.add(d2.getValue(Todo::class.java)!!)
        }
        adpt.notifyDataSetChanged()
    }

    return adpt
}