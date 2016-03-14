package teammates.common.datatransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.common.util.Sanitizer;
import teammates.common.util.StringHelper;

public class FeedbackRankOptionsResponseDetails extends FeedbackRankResponseDetails {
    private static final int CONST_INVALID_RESPONSE = -1;
    private List<Integer> answers;
    
    public FeedbackRankOptionsResponseDetails() {
        super(FeedbackQuestionType.RANK_OPTIONS);
    }
    
    @Override
    public void extractResponseDetails(FeedbackQuestionType questionType, 
                                       FeedbackQuestionDetails questionDetails, 
                                       String[] answer) {
        List<Integer> rankAnswer = new ArrayList<Integer>();
        for (int i = 0; i < answer.length ; i++){
            try {
                rankAnswer.add(Integer.parseInt(answer[i]));
            } catch (NumberFormatException e) {
                rankAnswer.add(Const.POINTS_NOT_SUBMITTED);
            }
        }
        FeedbackRankOptionsQuestionDetails rankQuestion = (FeedbackRankOptionsQuestionDetails) questionDetails;
        this.setRankResponseDetails(rankAnswer, rankQuestion.options);
    }

    /**
     * @return List of sorted answers, with uninitialised values filtered out
     */
    public List<Integer> getFilteredSortedAnswerList() {
        List<Integer> filteredAnswers = new ArrayList<>();
        
        for (int answer : answers) {
            if (answer != Const.POINTS_NOT_SUBMITTED) {
                filteredAnswers.add(answer);
            }
        }
        
        Collections.sort(filteredAnswers);
        return filteredAnswers;
    }
    
    public List<Integer> getAnswerList() {
        return new ArrayList<>(answers);
    }
    
    @Override
    public String getAnswerString() {
        String listString = getFilteredSortedAnswerList().toString(); //[1, 2, 3] format
        if (answers.get(0) == CONST_INVALID_RESPONSE) {
            return Const.INVALID_RESPONSE;
        }
        return listString.substring(1, listString.length() - 1); //remove []
    }

    @Override
    public String getAnswerHtml(FeedbackQuestionDetails questionDetails) {
        FeedbackRankOptionsQuestionDetails rankQuestion = (FeedbackRankOptionsQuestionDetails) questionDetails;
        
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<ul>");
        
        if (answers.get(0) == CONST_INVALID_RESPONSE) {
            htmlBuilder.append("<li>" + Const.INVALID_RESPONSE + "</li>");
        } else {
            SortedMap<Integer, List<String>> orderedOptions = generateMapOfRanksToOptions(rankQuestion);
            
            for (Entry<Integer, List<String>> rankAndOption : orderedOptions.entrySet()) {
                Integer rank = rankAndOption.getKey();
                if (rank == Const.POINTS_NOT_SUBMITTED) {
                    continue;
                }
                
                List<String> optionsWithGivenRank = rankAndOption.getValue();
                for (String option : optionsWithGivenRank) {
                    htmlBuilder.append("<li>");
                    htmlBuilder.append(Sanitizer.sanitizeForHtml(rank.toString()));
                    htmlBuilder.append(": ");
                    htmlBuilder.append(option);
                    htmlBuilder.append("</li>");
                }
            }
        }
        htmlBuilder.append("</ul>");
        return htmlBuilder.toString();
    }

    @Override
    public String getAnswerCsv(FeedbackQuestionDetails questionDetails) {
        FeedbackRankOptionsQuestionDetails rankQuestion = (FeedbackRankOptionsQuestionDetails) questionDetails;
        
        SortedMap<Integer, List<String>> orderedOptions = generateMapOfRanksToOptions(rankQuestion);
        
        StringBuilder csvBuilder = new StringBuilder();
        
        for (int rank = 1; rank <= rankQuestion.options.size(); rank ++) {
            if (!orderedOptions.containsKey(rank)) {
                csvBuilder.append(",");
                continue;
            }
            List<String> optionsWithGivenRank = orderedOptions.get(rank);
            
            String optionsInCsv = Sanitizer.sanitizeForCsv(StringHelper.toString(optionsWithGivenRank, ", "));
            
            csvBuilder.append(optionsInCsv + ",");
        }

        csvBuilder.deleteCharAt(csvBuilder.length() - 1); // remove last comma
        return csvBuilder.toString();
    }

    private SortedMap<Integer, List<String>> generateMapOfRanksToOptions(
                                    FeedbackRankOptionsQuestionDetails rankQuestion) {
        SortedMap<Integer, List<String>> orderedOptions = new TreeMap<>();
        for (int i = 0 ; i < answers.size() ; i++) {
            String option = rankQuestion.options.get(i);
            Integer answer = answers.get(i);
            
            if (!orderedOptions.containsKey(answer)) {
                orderedOptions.put(answer, new ArrayList<String>());
            }
            List<String> optionsWithGivenRank = orderedOptions.get(answer);
            optionsWithGivenRank.add(option);
        }
        return orderedOptions;
    }

    private void setRankResponseDetails(List<Integer> answers, List<String> options) {
        this.answers = answers;
    
        Assumption.assertEquals("Rank question: number of responses does not match number of options. " 
                                        + answers.size() + "/" + options.size(), 
                                answers.size(), options.size());
        
    }

}
