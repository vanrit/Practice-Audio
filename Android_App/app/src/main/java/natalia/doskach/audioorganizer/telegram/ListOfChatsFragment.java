package natalia.doskach.audioorganizer.telegram;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListOfChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListOfChatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ChatTGAdapter a;
    RecyclerView list;
    ArrayList<ChatItems> ai;
    ProgressBar pb;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListOfChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListOfChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListOfChatsFragment newInstance(String param1, String param2) {
        ListOfChatsFragment fragment = new ListOfChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
             ai= (ArrayList<ChatItems>)bundle.getSerializable("chats");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_of_chats, container, false);
        list = v.findViewById(R.id.chatListTD);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(list.getContext().getResources().getDrawable(R.drawable.sk_line_divider));
        list.addItemDecoration(dividerItemDecoration);
        a = new ChatTGAdapter(getContext(),ai);
        list.setAdapter(a);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        a.notifyDataSetChanged();
        return v;
    }

    public void getAudios(String chatID) throws InterruptedException {
        Log.i("info","send code");
        Thread.sleep(2000);
        pb.setVisibility(View.INVISIBLE);

    }
}