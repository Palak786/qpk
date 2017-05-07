package com.qpk;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class History extends Fragment {
    private List<Transaction> transactionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TransactionAdapter mAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ;

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mAdapter = new TransactionAdapter(transactionList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Transaction transaction = transactionList.get(position);
                //Toast.makeText(getApplicationContext(), transaction.getLocation() + " is selected!", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                // Setting Dialog Title
                alertDialog.setTitle("Cancel Booking !");

                // Setting Dialog Message
                alertDialog.setMessage("Do you want to cancel booking");

                // On pressing Settings button
                alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                // on pressing cancel button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

// Showing Alert Message
                alertDialog.show();


            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        prepareTransactionData();
        return view;
    }

    private void prepareTransactionData() {
        Transaction transaction = new Transaction("Mad Max: Fury Road", "Action & Adventure", "2015","adshsh","dhjsyf");
        transactionList.add(transaction);

        transaction = new Transaction("Inside ", "Animation, Kids & Family", "2015", "ffyef", "fjegf");
        transactionList.add(transaction);

        transaction = new Transaction("Star Wars: Episode VII - The Force Awakens", "Action", "2015", "sfeyfe", "hdgyg");
        transactionList.add(transaction);

        transaction = new Transaction("Shaun the Sheep", "Animation", "2015", "bsjfee", "fgkhgirg");
        transactionList.add(transaction);

        transaction = new Transaction("The Martian", "Science Fiction & Fantasy", "2015", "fbjfgef", "fnekgheg");
        transactionList.add(transaction);





        mAdapter.notifyDataSetChanged();
    }

}
