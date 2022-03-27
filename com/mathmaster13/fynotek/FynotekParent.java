package com.mathmaster13.fynotek;
import com.pushtorefresh.javac_warning_annotation.Warning;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 A class for handling words in Fynotek, a conlang by mochaspen, in both its modern and old form. The parent class of <code>FynotekWord</code> and <code>OldFynotekWord</code>, containing all shared code between the two.
 @author mathmaster13
 @since 1.0
 */
public abstract sealed class FynotekParent permits FynotekWord, OldFynotekWord {
    /** The part of a Fynotek word before its final vowel or diphthong.
     @see #FynotekParent(String)
     */
    @NotNull
    public final String beginning;
    /** A Fynotek word's final vowel or diphthong.
     @see #FynotekParent(String)
     @see #ablaut(Ablaut)
     */
    @NotNull
    public final String vowels;
    /** The part of a Fynotek word after its final vowel or diphthong.
     @see #FynotekParent(String)
     */
    @NotNull
    public final String end;
    /**
     * Represents the case or tense that this word is marked with.
     * A <code>null</code> value represents a word's root form.
     @see #isMarked()
     @see #match(FynotekParent)
     @see FynotekWord#nounCase(FynotekWord.Case)
     @see #verbTense(Tense)
     */
    @Nullable
    public final Inflection inflection; // This class expects you to only create objects from root words, not marked forms. Create marked words with nounCase(), verbTense(), or match(), and the method will mark the word as such.

    // Constants
    private static final char[] vowelList = { 'a', 'e', 'i', 'o', 'u', 'y' };
    /** A list of all stops in Fynotek, in its modern or old form. Used internally in <code>isValidSequence</code>.
     @see #isValidSequence(String, String, byte, boolean)
     */
    protected static final char[] stopList = { 'p', 't', 'k', '\'' };

    /**
     Returns a String representation of this word.
     @return String representation of this word.
     */
    @Override
    public @NotNull String toString() {
        return (beginning + vowels + end);
    }

    /**
     * Checks loose value-based equality of this word and an Object.
     * If <code>o</code> has the same String representation as this word, and is an instance of the same class, this function returns <code>true</code>.
     * @param o the object with which to compare loosely.
     * @return <code>true</code> if this object has the same String representation as <code>o</code>; false otherwise.
     */
    public boolean looseEquals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FynotekParent that = (FynotekParent) o;

        if (!beginning.equals(that.beginning)) return false;
        if (!vowels.equals(that.vowels)) return false;
        return end.equals(that.end);
    }

    /**
     * Checks strict value-based equality of this word and an Object.
     * If two words have the same string representation but different inflections, they will not be considered equal.
     * For example, the word "tau" marked in both the hypothetical future and hypothetical gnomic tenses is "tuu", but since those two words are marked with different tenses, they are not considered equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this.looseEquals(o)) return false;
        FynotekParent that = (FynotekParent) o;
        return Objects.equals(inflection, that.inflection);
    }

    @Override
    public int hashCode() {
        int result = beginning.hashCode();
        result = 31 * result + vowels.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + (inflection != null ? inflection.hashCode() : 0);
        return result;
    }

    // Public constructors
    /**
     Converts a String into a FynotekParent. Leading and trailing whitespace is ignored (the <code>String.trim()</code> method is called on <code>word</code>).
     @param word word to be converted to a FynotekParent.
     */
    public FynotekParent(@NotNull String word) {
        this(word, null);
    }

    // Private constructors
    /**
     Creates a FynotekParent and sets its parameters to those provided.
     @param beginning the part of a word before its final vowel or diphthong.
     @param vowels a word's final vowel or diphthong.
     @param end the part of a word after its final vowel or diphthong.
     @param inflection this word's inflection, or <code>null</code> if it does not have one.
     @see #beginning
     @see #vowels
     @see #end
     @see #inflection
     */
    protected FynotekParent(@NotNull String beginning, @NotNull String vowels, @NotNull String end, @Nullable Inflection inflection) {
        this.beginning = beginning;
        this.vowels = vowels;
        this.end = end;
        this.inflection = inflection;
    }

    protected FynotekParent(@NotNull String[] word, @Nullable Inflection inflection) {
        this(word[0], word[1], word[2], inflection);
    }

    /**
     Creates a new FynotekParent from an existing FynotekParent.
     @param word the word to be copied to the new FynotekParent.
     */
    protected FynotekParent(@NotNull FynotekParent word) {
        beginning = word.beginning;
        vowels = word.vowels;
        end = word.end;
        inflection = word.inflection;
    }

    /**
     Converts a String into a FynotekParent, and marks the word as having the specified ablaut.  Leading and trailing whitespace is ignored (the <code>String.trim()</code> method is called on <code>word</code>), and the word will always be converted to lowercase.
     @param word word to be converted to a FynotekParent.
     @param inflection this word's inflection, or <code>null</code> if it does not have one.
     */
    protected FynotekParent(@NotNull String word, @Nullable Inflection inflection) {
        word = word.trim().toLowerCase(); // TODO: Change implementation to preserve capitalization (maybe?)
        this.inflection = inflection;
        if (word.isEmpty()) { // If you want to re-add the null check, change the condition to (word == null || word.isEmpty())
            beginning = vowels = end = "";
        } else if (word.length() == 1) {
            if (isVowel(word.charAt(0))) {
                vowels = word;
                beginning = end = "";
            } else {
                end = word;
                beginning = vowels = "";
            }
        } else {
            int vowelIndex = 0;
            int vowelLength = 0;
            for (int i = word.length() - 1; i >= 0; i--) {
                if (isVowel(word.charAt(i))) {
                    if (isVowel(word.charAt(i - 1))) {
                        vowelIndex = i - 1;
                        vowelLength = 2;
                    } else {
                        vowelIndex = i;
                        vowelLength = 1;
                    }
                    break;
                }
            }
            beginning = word.substring(0, vowelIndex);
            vowels = word.substring(vowelIndex, vowelIndex + vowelLength);
            end = word.substring(vowelIndex + vowelLength);
        }
    }


    // Internal-use methods
    /**
     Marks this word with the specified ablaut.
     @param vowel the ablaut to mark the word as.
     @return this word marked with the specified ablaut.
     @see #inflection
     */
    protected abstract @NotNull String[] ablaut(@NotNull Ablaut vowel);

    @Warning("This word was marked previously for case or tense, and should not be marked again.")
    protected final void previouslyMarkedWarning() {}

    /**
     Checks if a given character is a vowel. Specifically,returns <code>true</code> if and only if the given character is <code>'a'</code>, <code>'e'</code>, <code>'i'</code>, <code>'o'</code>, <code>'u'</code>, or <code>'y'</code>.
     @param letter the character to be checked for whether it is a vowel or not.
     @return <code>true</code> if and only if <code>letter</code> is a vowel.
     */
    protected static boolean isVowel(char letter) {
        for (char i : vowelList) if (letter == i) return true;
        return false;
    }

    private static boolean isStop_internal(char letter) {
        for (char i : stopList) {
            if (letter == i)
                return true;
        }
        return false;
    }

    /**
     A back-end for the <code>isValidSequence</code> functions in this class' subclasses. As such, implementation details will not be provided.
     @return <code>true</code> if <code>sequence</code> is a valid sequence, and <code>false</code> if not.
     @see OldFynotekWord#isValidSequence(String)
     @see FynotekWord#isValidSequence(String)
     */
    protected static boolean isValidSequence(@NotNull String sequence, @NotNull String regex, byte maxConsonants, boolean checkForSameConsonants) { // If stuff goes wrong try changing the byte to an int. Also, !checkForSameConsonants is used to check for if you should apply the special end cases for consonants.
        sequence = sequence.toLowerCase().trim();

        // Blank string check
        if (sequence.isEmpty())
            return false;

        // Orthographic validity check
        if (!sequence.replaceAll(regex, "").isEmpty())
            return false;

        // Check for a multiple-word sequence
        final String[] wordArray = sequence.split("\\s+");
        if (wordArray.length == 0)
            return false;
        if (wordArray.length > 1) {
            boolean output = true;
            for (String j : wordArray)
                output = (isValidSequence(j, regex, maxConsonants, checkForSameConsonants) && output);
            return output;
        }

        // Phonotactic vallidity check
        int i = 0;
        while (i < sequence.length()) {
            char testChar = sequence.charAt(i);
            if (isVowel(testChar)) {
                if (i > sequence.length() - 3)
                    return true; // If we get to this point, no VVV can occur.
                // VVV check
                int j;
                for (j = i + 1; j < sequence.length(); j++)
                    if (!isVowel(sequence.charAt(j)))
                        break;
                if (j - i > 2)
                    return false;
                i = j;

            } else {
                // Consonant check
                int j;
                for (j = i + 1; j < sequence.length(); j++)
                    if (isVowel(sequence.charAt(j)))
                        break;
                if (j - i + (!checkForSameConsonants && (i == 0 || j == sequence.length()) ? 1 : 0) > maxConsonants)
                    return false;

                // Same character check, if applicable
                if (checkForSameConsonants) {
                    char checkForSameChar = testChar;
                    for (int k = i + 1; k < j; k++) {
                        char c = sequence.charAt(k);
                        if (c == checkForSameChar)
                            return false;
                        checkForSameChar = c;
                    }
                }

                // Stop+Stop check
                boolean stopCheck = isStop_internal(testChar);
                for (int l = i + 1; l < j; l++) {
                    boolean currentCharIsStop = isStop_internal(sequence.charAt(l));
                    if (stopCheck && currentCharIsStop)
                        return false;
                    stopCheck = currentCharIsStop;
                }

                i = j;
            }
        }
        return true;
    }

    // Public methods

    /**
     * Returns the {@link Ablaut} that this word is marked with, or <code>null</code> if it is unmarked.
     * @return the Ablaut that this word is marked with
     * @see Ablaut
     * @since 2.0
     */
    public @Nullable Ablaut getAblaut() {
        if (inflection == null) return null;
        return inflection.getAblaut();
    }

    /**
     Returns this FynotekParent inflected for the verb tense specified by <code>tenseOfVerb</code>.
     This function should be called before any suffix functions, not after.
     If a word has previously been marked for case or tense, it usually should not be marked again.
     If this function is called on a marked word, a warning will be generated, and there is no guarantee for the result.
     @param tense the verb tense to inflect this FynotekParent for.
     @return this FynotekParent inflected for the specified verb tense.
     @see #match(FynotekParent)
     */
    public abstract @NotNull FynotekParent verbTense(@NotNull Tense tense);

    /**
     Returns this word inflected for the same case or tense as <code>word</code>.
     This function should be called before any suffix functions, not after.
     If a word has previously been marked for case or tense, it usually should not be marked again.
     If this function is called on a marked word, a warning will be generated, and there is no guarantee for the result.
     @param word the FynotekParent to match this word's inflection with.
     @return this word inflected for the same case or tense as <code>word</code>.
     @see #verbTense(Tense)
     */
    public abstract @NotNull FynotekParent match(@NotNull FynotekParent word);

    /**
     Returns whether this FynotekParent is marked or not. Specifically, returns <code>(markVowel != '\u0000')</code>. <b>Be careful:</b> this means that non-hypothetical present-tense verbs will return <code>false</code>, and in <code>FynotekWord</code> specifically nominative-case nouns will also return <code>false</code>, and "folo" in the accusative case (if "folo" is not a proper noun) will return <code>true</code>.
     @return <code>true</code> if this FynotekParent has been marked by ablaut or a proper noun suffix, and <code>false</code> if it has not been.
     @see #inflection
     */
    public final boolean isMarked() {
        return (inflection != null);
    }

    /**
     Returns this word with a suffix added to mark the first, second, or third person.
     @param person the person to mark this word for.
     @return this word with a suffix added to mark the first, second, or third person.
     @see #verbTense(Tense)
     */
    public @NotNull abstract FynotekParent personSuffix(@NotNull Person person);

    /**
     * Represents Fynotek ablaut. If a field is not applicable to a particular form of ablaut, a null character (<code>\u0000</code>) is used.
     * @since 2.0
     */
    public enum Ablaut {
        /**
         * Represents a word that has been inflected for a case or tense, but the inflection is an "implied" form of a word.
         * Nominative-case nouns and present-tense verbs fall into this category.
         * This constant does <i>not</i> represent the root form of a word (a form which is not inflected at all)—that is represented by <code>null</code>.
         */
        DEFAULT('\u0000', '\u0000'),
        A('a', 'e'), E('e', 'a'), I('i', 'y'),
        Y('y', 'i'), O('o', 'u'), U('u', 'o'), REDUPLICATION('\u0000', '\u0000');

        protected final char asChar;
        protected final char ablautPair;

        Ablaut(char asChar, char ablautPair) {
            this.asChar = asChar;
            this.ablautPair = ablautPair;
        }
    }

    /**
     * Represents the tense of a Fynotek verb. Tenses prefixed with <code>HYP_</code> are hypothetical tenses.
     * @since 2.0
     */
    public enum Tense implements Inflection {
        PRESENT(Ablaut.DEFAULT), PAST(Ablaut.I), FUTURE(Ablaut.O), GNOMIC(Ablaut.Y), // gets converted to HYP_GNOMIC in old fynotek
        HYP_PRESENT(Ablaut.A), HYP_PAST(Ablaut.E), HYP_FUTURE(Ablaut.U), HYP_GNOMIC(Ablaut.REDUPLICATION);

        @NotNull
        private final Ablaut ablaut;

        @Override
        public @NotNull Ablaut getAblaut() {
            return ablaut;
        }

        Tense(@NotNull Ablaut ablaut) {
            this.ablaut = ablaut;
        }
    }

    /**
     * Represents the concept of person on a Fynotek verb.
     * @see #personSuffix(Person)
     * @since 2.0
     */
    public enum Person {
        /** Represents a first-person verb form. */
        P1(null),
        /** Represents a second-person verb form. */
        P2("a"),
        /** Represents a third-person verb form. */
        P3("o");

        @Nullable
        protected final String suffix;

        Person(@Nullable String suffix) {
            this.suffix = suffix;
        }
    }
}