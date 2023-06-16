import json
import requests

def test_persian_localhost():
    payload = {"text": "زبان فارسی از زبان کهن تر پارسی میانه (یا پهلوی) آمده که آن نیز خود از پارسی باستان سرچشمه گرفته است.",
               "lang": "fa"}
    response = requests.post("http://localhost:8088/analyze", json=payload)
    assert str(response.status_code).startswith('2')
    parsed_response = json.loads(response.content.decode('utf-8'))
    assert len(parsed_response) == 1, f"unexpected number of sentences: {len(parsed_response)}"
    assert len(parsed_response[0]) == 24, f"unexpected sentence length: {len(parsed_response[0])}"


if __name__ == "__main__":
    test_persian_localhost()
