package com.qpk;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private List<Transaction> transactionList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView transid,date,time, vehicleno,location;

        public MyViewHolder(View view) {
            super(view);
            transid = (TextView) view.findViewById(R.id.transid);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            vehicleno = (TextView) view.findViewById(R.id.vehicleno);
            location = (TextView) view.findViewById(R.id.location);

        }
    }


    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.transid.setText(transaction.getTransactionid());
        holder.date.setText(transaction.getDate());
        holder.time.setText(transaction.getTime());
        holder.vehicleno.setText(transaction.getVehivleno());
        holder.location.setText(transaction.getLocation());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
