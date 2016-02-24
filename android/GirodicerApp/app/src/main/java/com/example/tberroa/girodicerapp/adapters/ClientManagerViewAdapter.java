package com.example.tberroa.girodicerapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.activities.PastInspectionsActivity;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClientManagerViewAdapter extends RecyclerView.Adapter<ClientManagerViewAdapter.ClientViewHolder> {

    private final Context context;
    private final List<Client> clients;

    public ClientManagerViewAdapter(Context context, List<Client> clients) {
        this.context = context;
        this.clients = clients;
    }

    public class ClientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageThumbnail;
        final TextView clientNumber;

        ClientViewHolder(View itemView){
            super(itemView);
            imageThumbnail = (ImageView) itemView.findViewById(R.id.client_thumbnail);
            clientNumber = (TextView) itemView.findViewById(R.id.client_number);
            imageThumbnail.setOnClickListener(this);
            clientNumber.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){

            // extract clicked client
            int i = getLayoutPosition();
            Client client = clients.get(i);

            // save client id to shared preference
            new ClientId().set(v.getContext(), client.id);

            // start past inspections activity
            context.startActivity(new Intent(v.getContext(), PastInspectionsActivity.class));
            if(context instanceof Activity){
                ((Activity)context).finish();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (clients != null){
            return clients.size();
        }
        else{
            return 0;
        }
    }

    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context c = viewGroup.getContext();
        View v = LayoutInflater.from(c).inflate(R.layout.element_client_thumbnail, viewGroup, false);
        return new ClientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClientViewHolder clientViewHolder, int i) {

        // set client number text
        String title = "Client " + Integer.toString(i + 1);
        clientViewHolder.clientNumber.setText(title);

        // get thumbnail url (TEST)
        String url = "http://www.lorensworld.com/wp-content/uploads/2013/06/shutterstock_107172992.jpg";

        // render thumbnail with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(clientViewHolder.imageThumbnail);
    }
}