/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.view.ui.spoof

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aurora.store.R
import com.aurora.store.data.providers.NativeDeviceInfoProvider
import com.aurora.store.data.providers.SpoofDeviceProvider
import com.aurora.store.data.providers.SpoofProvider
import com.aurora.store.databinding.FragmentGenericRecyclerBinding
import com.aurora.store.util.Log
import com.aurora.extensions.toast
import com.aurora.store.view.epoxy.views.preference.DeviceViewModel_
import com.aurora.store.view.ui.commons.BaseFragment
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import java.util.*


class DeviceSpoofFragment : BaseFragment() {

    private lateinit var B: FragmentGenericRecyclerBinding
    private lateinit var spoofProvider: SpoofProvider

    private var properties: Properties = Properties()

    companion object {
        @JvmStatic
        fun newInstance(): DeviceSpoofFragment {
            return DeviceSpoofFragment().apply {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        B = FragmentGenericRecyclerBinding.bind(
            inflater.inflate(
                R.layout.fragment_generic_recycler,
                container,
                false
            )
        )

        properties = NativeDeviceInfoProvider(requireContext()).getNativeDeviceProperties()
        spoofProvider = SpoofProvider.with(requireContext())

        return B.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (spoofProvider.isDeviceSpoofEnabled())
            properties = spoofProvider.getSpoofDeviceProperties()

        task {
            fetchAvailableDevices()
        } successUi {
            updateController(it)
        } failUi {
            Log.e("Could not get spoof device properties")
        }
    }

    private fun updateController(locales: List<Properties>) {
        B.recycler.withModels {
            setFilterDuplicates(true)
            locales.forEach {
                add(
                    DeviceViewModel_()
                        .id(it.hashCode())
                        .markChecked(properties == it)
                        .checked { _, checked ->
                            if (checked) {
                                properties = it
                                saveSelection(it)
                                requestModelBuild()
                            }
                        }
                        .properties(it)
                )
            }
        }
    }

    private fun fetchAvailableDevices(): List<Properties> {
        return SpoofDeviceProvider.with(requireContext()).availableDevice
    }

    private fun saveSelection(properties: Properties) {
        requireContext().toast(R.string.spoof_apply)
        spoofProvider.setSpoofDeviceProperties(properties)
    }
}