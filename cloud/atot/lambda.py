from whispercpp import Whisper

def handler(event, context):
    w = Whisper.from_pretrained("tiny.en")
    result = w.transcribe("./audio.mp3")
    return {
        'statusCode': 200,
        'body': json.dumps({'text': result})
    }


# I will be uploading to AWS S3 because if the thing fails thats ok, and I get to keep my data.
# Rather then sending the raw text back, I will put it into a file in S3
# Then when that is updated, it will call anotehr lambda which will query claude to parse it and update
# a md file for that day? The issue with this however is, how do i get that into obsidian. Perhaps it would be better
# to make a slug and then check the status so I can pull it at any given point?
# Maybe I should just return the raw text, and let tasker call some api which does the parsing and returns the text.

# import whisper
# import json

# def handler(event, context):
#     try:
#         model = whisper.load_model("tiny.en")
#         result = model.transcribe("./audio.mp3")
        
#         return {
#             'statusCode': 200,
#             'body': json.dumps({
#                 'text': result["text"]
#             })
#         }
#     except Exception as e:
#         return {
#             'statusCode': 500,
#             'body': json.dumps({
#                 'error': str(e)
#             })
#         }