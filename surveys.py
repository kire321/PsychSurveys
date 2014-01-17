from random import random
from schemas import allOf, SurveyResponse
from datetime import datetime, timedelta


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


def getQuestion(client):
    awake = client.get("sound", 0) > 10000 or client["accel"] > .3
    if awake:# and random() < .1:
        responses = filter(lambda response: response.clientID == client["clientID"], allOf(SurveyResponse))
        if len(responses) == 0:
            answeredRecently = False
        else:
            answeredRecently = datetime.now() - responses[-1].time < timedelta(0, 8 * 60 * 60)
        if not answeredRecently:
            return q1

q1 = Question("Which of the following best describes what you are currently doing?")
q2 = Question("Are you currently:")
q3 = Question("How would you describe your current environment:")
q4 = Question("Are you currently:")
emotions = [Question(("Indicate how strongly are you currently experiencing"
        " this emotion using a rating scale of 1 to 5 where 1 = not at all"
        " and 5 = very strong:\n\n%s\n" % emotion)) for emotion in [
            "Content", "Anxious", "Excited", "Lethargic",
            "Surprised", "Happy", "Sad", "Quiet"]]
finish = Question("Thanks for taking the survey.")

q1.addAnswer("Studying/Class/Research/Other Academic Activity", q2)
q1.addAnswer("Socializing/Talking/Interacting with Friends of Family", q4)
q1.addAnswer("Non-academic work", q4)
q1.addAnswer("Sports/Exercise/Extra-Curricular Activity", q4)
q1.addAnswer("Other", q4)

q2.addAnswer("In class", finish)
q2.addAnswer("Studying or preparing for class", q3)
q2.addAnswer("Conducting Research", q3)
q2.addAnswer("Engaging in Other Academic Activity", q3)

q3.addAnswer("Very quiet -- no noise besides white noise or occasional muted sounds.", q4)
q3.addAnswer("Moderately quiet -- no noise louder than faint, muffled conversation.", q4)
q3.addAnswer("Somewhat quiet -- steady sound, but of a soothing nature (e.g. soft music)", q4)
q3.addAnswer("Somewhat loud -- steady sound of a regular volume (e.g. normal volume conversations)", q4)
q3.addAnswer("Moderately loud -- steady sound like music.conversation/TV occaisonally punctuated by something much louder", q4)
q3.addAnswer("Very loud -- steady, loud sound from multiple sources", q4)

q4.addAnswer("Alone", emotions[0])
q4.addAnswer("With one other person", emotions[0])
q4.addAnswer("In a small group (3-5)", emotions[0])
q4.addAnswer("In a medium group (6-8)", emotions[0])
q4.addAnswer("In a large group (9+)", emotions[0])

for i in range(len(emotions) - 1):
    for j in range(1, 6):
        emotions[i].addAnswer(str(j), emotions[i + 1])

for i in range(1, 6):
    emotions[-1].addAnswer(str(i), finish)
