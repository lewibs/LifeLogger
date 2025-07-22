# I will be uploading to AWS S3 because if the thing fails thats ok, and I get to keep my data.
# Rather then sending the raw text back, I will put it into a file in S3
# Then when that is updated, it will call anotehr lambda which will query claude to parse it and update
# a md file for that day? The issue with this however is, how do i get that into obsidian. Perhaps it would be better
# to make a slug and then check the status so I can pull it at any given point?
# Maybe I should just return the raw text, and let tasker call some api which does the parsing and returns the text.

I will use vosk instead?... I will use vosk instead... obviously because the cost is high, I may try saving the audio data to some location for training data. Would people let me colect their data?
