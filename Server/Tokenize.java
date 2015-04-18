/*
Michael D'Alessio
MUD 
Senior Project
Professor Miller's class
Spring 2015
*/

import java.util.Arrays;
import java.util.List;


public class Tokenize {

    public final String first;//first word of the string
    public final String remainder;//remainder of string

    /*
NAME

        Tokenize-Take an arbitrary space seperated string and splits it into two pieces
 *                [command] [remainder.... .... ....]

SYNOPSIS
            Test        --> Text to tokenize!


DESCRIPTION

        This function will set the split variables first and remainder
        so that in the parse function in the server we can split off
        the username, the command, and a message if we have one.
        This is super important to getting commands to work correctly
        in my program, took a little time to get figured out! 


RETURNS

        None

AUTHOR

        Michael D'Alessio

DATE

        9:40pm 4/17/2015
/**/


    public Tokenize(String text) {
        List<String> splits = Arrays.asList(text.trim().split(" "));

        if (splits.size() >= 1 && splits.get(0).length() > 0) {

            this.first = splits.get(0);
            this.remainder = text.substring(first.length()).trim();
        } 

        else {
            this.first = null;
            this.remainder = null;
        }
    }

}