package com.atech.linksaver.utils

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class DialogModel(
    var title: String,
    var message: String,
    var positiveText: String,
    var negativeText: String,
    var positiveAction: (DialogInterface) -> Unit = { },
    var negativeAction: (DialogInterface) -> Unit = { }
)


fun Context.universalDialog(
    model: DialogModel
) = this.apply {
    MaterialAlertDialogBuilder(this)
        .setTitle(model.title)
        .setMessage(model.message)
        .setPositiveButton(model.positiveText) { dialog, _ ->
            model.positiveAction(dialog)

        }
        .setNegativeButton(model.negativeText) { dialog, _ ->
            model.negativeAction(dialog)
        }
        .show()
}

val DELETE_DIALOG = DialogModel(
    title = "Delete",
    message = "Are you sure you want to delete this link?",
    positiveText = "Yes",
    negativeText = "No",
    negativeAction = { dialog ->
        dialog.dismiss()
    }
)