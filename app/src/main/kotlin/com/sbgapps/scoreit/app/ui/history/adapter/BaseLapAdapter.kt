/*
 * Copyright 2019 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.app.ui.history.adapter

import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.sbgapps.scoreit.app.R
import com.sbgapps.scoreit.core.widget.BaseViewHolder
import com.sbgapps.scoreit.core.widget.ItemAdapter

open class BaseLapAdapter(@LayoutRes layoutId: Int) : ItemAdapter(layoutId) {

    @CallSuper
    override fun onBindViewHolder(viewHolder: BaseViewHolder) {
        viewHolder.itemView.setOnClickListener {
            Toast.makeText(it.context, R.string.history_lap_click_hint, Toast.LENGTH_SHORT).show()
        }
    }
}