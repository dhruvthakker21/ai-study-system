import os
from flask import Flask, request, jsonify
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api._errors import (
    IpBlocked,
    TranscriptsDisabled,
    NoTranscriptFound,
    VideoUnavailable
)

app = Flask(__name__)


@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok'})

@app.route('/transcript', methods=['POST'])
def get_transcript():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({'error': 'Request body must be valid JSON'}), 400

    video_id = data.get('videoId')
    if not video_id:
        return jsonify({'error': 'videoId is required'}), 400

    try:
        # New API — create instance first, then call fetch
        ytt = YouTubeTranscriptApi()
        transcript_list = ytt.list(video_id)

        try:
            transcript = transcript_list.find_transcript(['en', 'en-US', 'hi'])
        except NoTranscriptFound:
            transcript = next(iter(transcript_list))

        data_parts = transcript.fetch()
        full_text = ' '.join([part.text for part in data_parts])

        return jsonify({
            'success': True,
            'videoId': video_id,
            'language': transcript.language,
            'transcript': full_text
        })

    except VideoUnavailable:
        return jsonify({'error': 'Video is invalid or private'}), 404
    except IpBlocked:
        return jsonify({'error': 'IP blocked by YouTube'}), 429
    except TranscriptsDisabled:
        return jsonify({'error': 'Transcripts disabled for this video'}), 404
    except NoTranscriptFound:
        return jsonify({'error': 'No transcripts found'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=False, threaded=True)
