from schemas import allOf, SurveyResponse
from surveys import Question
from collections import defaultdict


def tabulate():
    indices = range(len(Question.questions))
    columns = map(lambda i: "Response to question #%s" % str(i + 1), indices)
    csv = "RespondentIDNum, %s\n" % ', '.join(columns)

    surveys = defaultdict(list)
    for response in allOf(SurveyResponse):
        surveys[response.survey].append(response)

    for survey in surveys.values():
        answers = ["No answer"] * len(Question.questions)
        for response in survey:
            answers[response.question] = str(response.answer + 1)
        csv += ', '.join([survey[0].clientID] + answers) + '\n'

    return csv

if __name__ == "__main__":
    print tabulate()
