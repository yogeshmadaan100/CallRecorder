package com.cb.callrecorder;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cb.callrecorder.adapter.SpeedDialAdapter;
import com.cb.callrecorder.R;

import java.io.File;

/**
 * Created by Jay Rambhia on 11/06/15.
 */
public class AllCallsFragment extends Fragment implements SpeedDialAdapter.AdapterInterface {

    private RecyclerView mRecyclerView;
    //private List<String> items;

    static Cursor c;
    HelperCallRecordings  hcr;
    LayoutInflater layoutInflater;
    SpeedDialAdapter mAdapter;
    private TextView default_text;
    public static AllCallsFragment newInstance() {
        return new AllCallsFragment();
    }

    public AllCallsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstaceState) {
        ViewGroup rootView = (ViewGroup)inflater
                .inflate(R.layout.fragment_speeddial_layout, parent, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        default_text = (TextView) rootView.findViewById(R.id.default_text);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hcr=new HelperCallRecordings(getActivity());

        c=hcr.display();
        mAdapter = new SpeedDialAdapter(layoutInflater,c,getActivity(),this);
        Log.e("all call count is ",""+c.getCount());
        if(c.getCount()==0)
        {
            default_text.setVisibility(View.VISIBLE);
        }
        else
            default_text.setVisibility(View.GONE);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }



    @Override
    public void deleteandset(int position) {
        c.moveToPosition(position);
        File file=new File(c.getString(2));

        HelperCallRecordings hcr=new HelperCallRecordings(CallApplication.getInstance());

        hcr.clearData(c.getString(c.getColumnIndex("Time")));

        c=hcr.display();


        mAdapter = new SpeedDialAdapter(layoutInflater,c,getActivity(),this);
        Log.e("received call count is ", "" + c.getCount());
        if(c.getCount()==0)
        {
            default_text.setVisibility(View.VISIBLE);
        }
        else
            default_text.setVisibility(View.GONE);
        mRecyclerView.setAdapter(mAdapter);
        hcr.closeDatabase();

        if(file.exists())
        {
            file.delete();
            Toast.makeText(CallApplication.getInstance(), "File deleted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(CallApplication.getInstance(), "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }
}
