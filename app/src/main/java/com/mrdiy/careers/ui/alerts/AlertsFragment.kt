package com.mrdiy.careers.ui.alerts

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrdiy.careers.MainActivity
import com.mrdiy.careers.databinding.FragmentAlertsBinding
import com.mrdiy.careers.ui.base.BaseFragment
import com.mrdiy.careers.data.repository.InboxRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AlertsFragment : BaseFragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setName("AlertsFragment")

        (requireActivity() as MainActivity).markAlertsRead()

        binding.tvUnreadCount.text = ""
        viewLifecycleOwner.lifecycleScope.launch {
            InboxRepository(requireContext()).alerts().onSuccess { alerts ->
                binding.alertsRecycler.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
                binding.alertsRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.alertsRecycler.adapter = AlertAdapter { }
                (binding.alertsRecycler.adapter as AlertAdapter).submitList(alerts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}