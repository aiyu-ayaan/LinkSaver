package com.atech.linksaver.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

inline fun addOnContextualMenuListener(
    crossinline onCreate: (ActionMode?, Menu?) -> Boolean,
    crossinline onPrepare: (ActionMode?, Menu?) -> Boolean = { _, _ -> false },
    crossinline onActionItemClicked: (ActionMode?, MenuItem?) -> Boolean = { _, _ -> false },
    crossinline onDestroy: (ActionMode?) -> Unit = { }
): ActionMode.Callback = object : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return onCreate(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return onPrepare(mode, menu)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return onActionItemClicked(mode, item)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroy(mode)
    }
}