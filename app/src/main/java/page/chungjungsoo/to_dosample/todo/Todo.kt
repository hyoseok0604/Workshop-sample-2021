package page.chungjungsoo.to_dosample.todo

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Todo(
    var uid : Int? = 0,
    var title : String? = "",
    var description : String? = "",
    var due : String? = "",
    var finished : Boolean = false
)