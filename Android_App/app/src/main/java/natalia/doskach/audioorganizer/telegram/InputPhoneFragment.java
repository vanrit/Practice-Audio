package natalia.doskach.audioorganizer.telegram;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.MainActivity;
import natalia.doskach.audioorganizer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InputPhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InputPhoneFragment extends Fragment {
    TextView prompt;
    TextInputEditText text;
    ProgressBar pb;
    Boolean isPhoneMode;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InputPhoneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InputPhoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InputPhoneFragment newInstance(String param1, String param2) {
        InputPhoneFragment fragment = new InputPhoneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPhoneMode = true;
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void sendCode(String toString, ProgressBar pb) {

    }

    private void changeLayoutToCode() {
        prompt.setText("Введите проверочный код из Telegram");
        text.setText("");
        isPhoneMode = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_input_phone, container, false);
        prompt = v.findViewById(R.id.prompt);
        text = v.findViewById(R.id.textInput);
        pb = v.findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_DONE){
                    Log.i("info","finished writing");
                    getActivity().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                    );
                    pb.setVisibility(View.VISIBLE);
                    if(isPhoneMode){
                        try {
                            Log.i("info","send phone");
                            Thread.sleep(2000);
                            pb.setVisibility(View.INVISIBLE);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        changeLayoutToCode();
                    }
                    else{
                        try {
                            Log.i("info","send code");
                            Thread.sleep(2000);
                            pb.setVisibility(View.INVISIBLE);
                            ArrayList<ChatItems> chats = new ArrayList<>();
                            chats.add(new ChatItems("1","Mum"));
                            chats.add(new ChatItems("1","Cat"));
                            chats.add(new ChatItems("1","Friend"));
                            ((TelegramActivity)getActivity()).changeFragmentToChats(chats);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }



                }
                return false;
            }
        });
        return v;
    }



}