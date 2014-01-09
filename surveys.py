class Question(object):
    def __init__(self, text):
        self.text = text
        self.answers = []

    def addAnswer(self, text):
        self.answers.append(text)

testQuestion = Question("Which of the following best describes what you are currently doing")
testQuestion.addAnswer("Studying/Class/Research/Other Academic Activity")
testQuestion.addAnswer("Socializing/Talking/Interacting with Friends of Family")
testQuestion.addAnswer("Non-academic work")
testQuestion.addAnswer("Sports/Exercise/Extra-Curricular Activity")
testQuestion.addAnswer("Other")
