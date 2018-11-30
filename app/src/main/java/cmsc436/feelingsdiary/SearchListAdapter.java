package cmsc436.feelingsdiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/* Adapter for mResultsList in SearchActivity */
public class SearchListAdapter extends BaseAdapter {

    private ArrayList<Entry> list = new ArrayList<>();
    private static LayoutInflater inflater;

    public SearchListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;
        ViewHolder holder;

        Entry entry = list.get(position);

        // ViewHolder stuff for better scrolling
        if (convertView == null) {
            holder = new ViewHolder();
            newView = inflater.inflate(R.layout.entry_list_layout, parent, false);

            holder.datetime = newView.findViewById(R.id.datetime);
            holder.moodRating = newView.findViewById(R.id.mood);
            holder.message = newView.findViewById(R.id.message_string);
            newView.setTag(holder);
        } else {
            holder = (ViewHolder) newView.getTag();
        }

        // Set appropriate data in the View
        holder.datetime.setText(R.string.datetime + entry.getDate());
        holder.moodRating.setText(R.string.mood_rating + entry.getRating());
        if (entry.getEntry().length() == 0) {
            holder.message.setText(R.string.message + "None");
        } else if (entry.getEntry().length() > 26) {
            holder.message.setText(R.string.message + entry.getEntry().substring(0, 26) + "...");
        } else {
            holder.message.setText(R.string.message + entry.getEntry());
        }

        return newView;
    }

    // ViewHolder for better scrolling
    private static class ViewHolder {
        TextView datetime;
        TextView moodRating;
        TextView message;
    }

    public void clearList() {
        list.clear();
        notifyDataSetInvalidated();
    }

    public void addList(List<Entry> list) {
        this.list.addAll(list);
        notifyDataSetInvalidated();
    }
}
