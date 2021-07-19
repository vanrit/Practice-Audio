package natalia.doskach.audioorganizer.telegram;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.AudioListAdapter;
import natalia.doskach.audioorganizer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListOfAudiosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListOfAudiosFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AudioTGAdapter a;
    RecyclerView list;
    ArrayList<AudioItems> ai;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListOfAudiosFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ListOfAudiosFragment newInstance(Bundle b) {
        ListOfAudiosFragment fragment = new ListOfAudiosFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ai= (ArrayList<AudioItems>)bundle.getSerializable("audios");
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_of_audios, container, false);
        list = v.findViewById(R.id.audioListTD);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(list.getContext().getResources().getDrawable(R.drawable.sk_line_divider));
        list.addItemDecoration(dividerItemDecoration);
        a = new AudioTGAdapter(getContext(),ai);
        list.setAdapter(a);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        a.notifyDataSetChanged();
        return v;
    }
}

