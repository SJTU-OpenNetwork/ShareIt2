package com.sjtuopennetwork.shareit.util.contactlist;

import java.util.Comparator;

public class LetterCompare implements Comparator<MyContactBean> {

    @Override
    public int compare(MyContactBean l, MyContactBean r) {
        if (l == null || r == null) {
            return 0;
        }
        if (l.index <= 0 && r.index <= 0) {
        } else if(l.index >0 && r.index >0) {
            int result = l.index - r.index;
            if (result > 0) {
                return 1;
            }
            if (result == 0) {
                return result;
            }
            if (result < 0) {
                return -1;
            }
        }else {
            int result = l.index - r.index;
            if (result > 0) {
                return -1;
            }
            if (result == 0) {
                return result;
            }
            if (result < 0) {
                return 1;
            }
        }

        String lhsSortLetters = l.letter;
        String rhsSortLetters = r.letter;
        if (lhsSortLetters == null || rhsSortLetters == null) {
            return 0;
        }
        return lhsSortLetters.compareTo(rhsSortLetters);
    }
}
