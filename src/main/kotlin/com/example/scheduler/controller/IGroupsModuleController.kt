package com.example.scheduler.controller

import com.example.scheduler.objects.Group

interface IGroupsModuleController {
    fun showGroups()
    fun getFields()
    fun showContextMenu(selectedRowIndex: Int, selectedItem: String)
    fun setOnGroupSelected()
    fun deleteGroup(groupToDelete: Group)
    fun addGroup()
    fun setListeners()
    fun setTexts()
}