package com.example.firestoreinsetprototype.Util

import com.example.firestoreinsetprototype.Model.ModelBase

object ArrayUtil {
    fun hasItem(arr: ArrayList<ModelBase>, item: ModelBase): ModelBase? {
        for(index in 0 until arr.size){
            if(item.id == arr[index].id)
                return arr[index]
        }
        return null
    }

}