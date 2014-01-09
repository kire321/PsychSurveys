class Question(object):

    questions = []

    def __init__(self, text):
        self.text = text
        self.answers = []
        self.idNum = len(Question.questions)
        Question.questions.append([])

    def addAnswer(self, text, nextQuestion):
        self.answers.append(text)
        Question.questions[self.idNum].append(nextQuestion)

testQuestion = Question("Which of the following best describes what you are currently doing?")
finish = Question("Thanks for taking the survey.")

testQuestion.addAnswer("Studying/Class/Research/Other Academic Activity", finish)
testQuestion.addAnswer("Socializing/Talking/Interacting with Friends of Family", finish)
testQuestion.addAnswer("Non-academic work", finish)
testQuestion.addAnswer("Sports/Exercise/Extra-Curricular Activity", finish)
testQuestion.addAnswer("Other", finish)
