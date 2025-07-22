import whisper

model = whisper.load_model("tiny.en")
result = model.transcribe("./audio.mp3")
print(result["text"])

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