
#FRONTEND WORK*******************************************************
from flask import Flask, render_template, request
app = Flask(__name__)

@app.route('/')
def index():
    return render_template('home.html')

@app.route('/scripts', methods=['GET', 'POST'])
def scripts():
    if request.method == 'POST':
        if "commonWords" in request.form:
            return render_template('commonwordsscripts.html')
        elif "lineSentiment" in request.form:
            return render_template('sentimentalscripts.html')

    return render_template('scripts.html')


@app.route('/tweets', methods=['GET', 'POST'])
def tweets():
    if request.method == 'POST':
        if "commonWordsTwitter" in request.form:
            return render_template('commonwordstwitter.html')
        elif "uniqueWords" in request.form:
            return render_template('uniquewordstwitter.html')
        elif "wordClouds" in request.form:
            return render_template('wordcloudstwitter.html')


    return render_template('tweets.html')
    
@app.route('/overview')
def overview():
    return render_template('overview.html') 