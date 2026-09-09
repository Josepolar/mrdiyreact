package com.mrdiy.careers.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrdiy.careers.databinding.FragmentMessagesBinding
import com.mrdiy.careers.model.Message

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messages = listOf(
            Message("1", "MR.D.I.Y. HR Team", "DIY", "Your interview is scheduled for tomorrow at 10:00 AM", "5 min ago", isRead = false, unreadCount = 2, jobId = "1"),
            Message("2", "Recruitment Bot", "RB", "Congratulations! Your application was shortlisted", "1 hr ago", isRead = false, unreadCount = 1, jobId = "2"),
            Message("3", "Store Manager - SM North", "SM", "Thank you for your application. We will review and get back to you.", "3 hrs ago", isRead = true, unreadCount = 0, jobId = "1"),
            Message("4", "HR Department", "HR", "Welcome to MR.D.I.Y.! Your onboarding is confirmed.", "Yesterday", isRead = true, unreadCount = 0)
        )

        val adapter = MessageAdapter(messages) { message ->
            message.jobId?.let { jobId ->
                val action = MessagesFragmentDirections.actionMessagesToJobDetail(jobId)
                findNavController().navigate(action)
            }
        }

        binding.messagesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        val unreadCount = messages.count { !it.isRead }
        binding.tvUnreadCount.text = if (unreadCount > 0) "$unreadCount unread" else ""

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}