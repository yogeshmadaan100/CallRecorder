package com.cb.callrecorder.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cb.callrecorder.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Jay Rambhia on 11/06/15.
 */
public class SpeedDialAdapter extends RecyclerView.Adapter<SpeedDialAdapter.ViewHolder> {

     public interface AdapterInterface{

            public void deleteandset(int position);

        }

    LayoutInflater inflater;

    public static Cursor c;
    public Context context;
    public AdapterInterface adapterInterface;
    char st_name;
    ImageButton opt ;
    int numberindex,timeindex,calltypeindex;
    ImageLoader imageLoader;
    DisplayImageOptions options;

    String []menu_items ={"Play","Delete","Send Message"};
    String []menu_items2 ={"Play","Delete","Send Message","Add To Contacts"};
    boolean temp=false;

    public SpeedDialAdapter(LayoutInflater inflater1,Cursor c1, Context context , AdapterInterface adapterInterface) {

        try
        {
            inflater=inflater1;
            c=c1;
            this.context=context;

            c.moveToFirst();
            numberindex=c.getColumnIndex("Number");
            timeindex=c.getColumnIndex("Time");
            calltypeindex=c.getColumnIndex("CallType");
            this.adapterInterface = adapterInterface;
            imageLoader=ImageLoader.getInstance();

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.def_img)
                    .showImageForEmptyUri(R.drawable.def_img)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(20))
                    .build();

            imageLoader.init(ImageLoaderConfiguration.createDefault(context));



        }catch(Exception e)
        {
            Log.e("intilisation adapter", "exception");
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.speeddial_item_layout,
                parent, false);

        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(SpeedDialAdapter.ViewHolder holder,final int position) {

        ArrayAdapter<String> adapter =new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line,menu_items);
        ArrayAdapter<String>adapter2 =new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line,menu_items2);
        c.moveToPosition(position);
        holder.opt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                PopupMenu popup = new PopupMenu(context, v);
                try {
                    if(getContactName(c.getString(numberindex))!=null)
                        popup.getMenuInflater().inflate(R.menu.popup1, popup.getMenu());
                    else
                        popup.getMenuInflater().inflate(R.menu.popup2, popup.getMenu());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        c.moveToPosition(position);
                        switch (item.getItemId()) {
                            case R.id.one:
                                try{
                                    Log.e("calling"+ c.getString(2), "play"+position);
                                    File file = new File(c.getString(2));
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                                    context.startActivity(intent);

                                }catch(Exception e)
                                {
                                    Log.e("play", "exception "+e);
                                }
                                break;
                            case R.id.two:
                                try{


                                    adapterInterface.deleteandset(position);
                                }catch(Exception e)
                                {

                                }
                                break;
                            case R.id.three:
                                String mypath=c.getString(2);
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("audio/*");
                                share.putExtra(Intent.EXTRA_TEXT, "I Recorded this audio using iManiac Call Recorder .\n https://play.google.com/store/apps/details?id=com.imaniac.callrecorder");
                                share.putExtra(Intent.EXTRA_STREAM,Uri.parse("file:///"+mypath));
                                context.startActivity(Intent.createChooser(share, "Share Sound File"));
                                break;
                            case R.id.four:
                                String snumber=c.getString(0);
                                String sname = null;
                                try {
                                    sname = getContactName(snumber);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                if(sname!=null)
                                {
                                    Toast.makeText(context, "Already in contacts", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                                    intent.putExtra(ContactsContract.Intents.Insert.PHONE,c.getString(0));
                                    context.startActivity(intent);
                                }
                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Log.e("number is", ""+numberindex);
        String sname= null;
        try {
            sname = getContactName(c.getString(numberindex));
        }catch(Exception e)
        {

        }
        String scalltype=c.getString(calltypeindex);

        //Setting image according to received, dialled or missed
        if(scalltype.equals("incoming"))
            holder.calltype.setBackgroundResource(R.drawable.sym_call_incoming);
        else if(scalltype.equals("outgoing"))
            holder.calltype.setBackgroundResource(R.drawable.sym_call_outgoing);
        else if(scalltype.equals("missed"))
            holder.calltype.setBackgroundResource(R.drawable.sym_call_missed);

        //Setting contact image
        Uri bmpUri = null;

        try {
            bmpUri=getContactPhoto(c.getString(numberindex));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(bmpUri!=null)
        {
            imageLoader.displayImage(bmpUri.toString(), holder.contactphoto, options);
        }
        else
        {
            Random r = new Random();
            int a = r.nextInt(255);
            holder.contactphoto.setBackgroundColor(Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            holder.contactphoto.setImageResource(R.drawable.def_img);
        }

        //Log.e("position", ""+position);
        //Log.e("name", sname);
        if(sname!=null)
            holder.name.setText(sname);
        else

        {

            holder.name.setText(c.getString(numberindex));
        }

        holder.number.setText(c.getString(numberindex));

        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm");
        String stime=sdf.format(new Date(Long.parseLong(c.getString(timeindex))));

        holder.time.setText(stime);

        sdf=new SimpleDateFormat("dd/MM/yyyy");
        String sdate=sdf.format(new Date(Long.parseLong(c.getString(timeindex))));

        holder.date.setText(sdate);
    }




    @Override
    public int getItemCount() {
        return c == null ? 0 : c.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        TextView name,number,time,date;
        ImageView calltype,contactphoto;
        ImageButton opt;
        RelativeLayout rl;
        public ViewHolder(View convertView) {
            super(convertView);
            this.itemView = itemView;
            rl =(RelativeLayout)convertView.findViewById(R.id.rl);
            name=(TextView)convertView.findViewById(R.id.name);
            number=(TextView)convertView.findViewById(R.id.number);
            time=(TextView)convertView.findViewById(R.id.time);
            date=(TextView)convertView.findViewById(R.id.date);
            calltype=(ImageView)convertView.findViewById(R.id.calltype);
            contactphoto=(ImageView)convertView.findViewById(R.id.contactphoto);
            opt=(ImageButton)convertView.findViewById(R.id.menu);
        }
    }
        public String getContactName(String snumber) throws Exception
        {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(snumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
            String contactName = null;
            if(cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        }

        public Uri getContactPhoto(String phoneNumber) throws Exception {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID },
                    null, null, null);

            long contactId = 0;

            if (cursor.moveToFirst()) {
                do {
                    contactId = cursor.getLong(cursor
                            .getColumnIndex(ContactsContract.PhoneLookup._ID));
                } while (cursor.moveToNext());
            }

            return getUserPictureUri(contactId);

        }

        private Uri getUserPictureUri(long id) throws Exception {
            Uri person = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, id);

            Uri picUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            try {
                InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                        context.getContentResolver(), picUri);
                is.close();
            } catch (FileNotFoundException e) {
                //Contact image does not exist
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }catch(Exception e)
            {
                //Log.d(" picture exception", "called");
                return null;
            }

            return picUri;
        }
}
