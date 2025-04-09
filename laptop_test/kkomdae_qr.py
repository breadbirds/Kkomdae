# ===============================
# 표준 라이브러리 및 외부 라이브러리 임포트
# ===============================
import sys
import os
import re
import subprocess
import logging
import time
import ctypes
from ctypes import wintypes
import json
from datetime import datetime
import requests
# 외부 라이브러리
from tkinter import messagebox
import ttkbootstrap as ttkb
from ttkbootstrap.constants import *
from PIL import Image, ImageTk, ImageFont, ImageDraw, ImageEnhance
import cv2
import win32com.client
import psutil
import qrcode
import threading

# ===============================
# Windows API 상수 및 구조체 정의
# ===============================ak
# 플랫폼에 따라 LRESULT, LONG_PTR 타입 결정
if ctypes.sizeof(ctypes.c_void_p) == 8:
    LRESULT = ctypes.c_longlong
    LONG_PTR = ctypes.c_longlong
else:
    LRESULT = ctypes.c_long
    LONG_PTR = ctypes.c_long

# Windows 메시지 상수
WM_NCDESTROY = 0x0082
WM_INPUT = 0x00FF
RID_INPUT = 0x10000003
GWL_WNDPROC = -4
RIDI_DEVICENAME = 0x20000007
RIM_TYPEKEYBOARD = 1
RIDEV_INPUTSINK = 0x00000100
RIDEV_NOLEGACY = 0x00000030  # legacy 메시지 차단
RIDEV_REMOVE = 0x00000001   # Raw Input 해제 플래그

RI_KEY_BREAK = 0x01
RI_KEY_E0 = 0x02

WM_DEVICECHANGE = 0x0219
DBT_DEVICEARRIVAL = 0x8000
DBT_DEVICEREMOVECOMPLETE = 0x8004
DBT_DEVTYP_DEVICEINTERFACE = 0x00000005

# user32 라이브러리 로드 및 함수 서명 지정
user32 = ctypes.windll.user32
user32.SetWindowLongPtrW.restype = LONG_PTR
user32.SetWindowLongPtrW.argtypes = [wintypes.HWND, wintypes.INT, LONG_PTR]
user32.CallWindowProcW.restype = LRESULT
user32.CallWindowProcW.argtypes = [LONG_PTR, wintypes.HWND, wintypes.UINT, wintypes.WPARAM, wintypes.LPARAM]


# Raw Input 관련 구조체 정의
class RAWINPUTDEVICE(ctypes.Structure):
    _fields_ = [
        ("usUsagePage", ctypes.c_ushort),
        ("usUsage", ctypes.c_ushort),
        ("dwFlags", ctypes.c_ulong),
        ("hwndTarget", ctypes.c_void_p)
    ]

class RAWINPUTHEADER(ctypes.Structure):
    _fields_ = [
        ("dwType", ctypes.c_uint),
        ("dwSize", ctypes.c_uint),
        ("hDevice", ctypes.c_void_p),
        ("wParam", ctypes.c_ulong)
    ]

class RAWKEYBOARD(ctypes.Structure):
    _fields_ = [
        ("MakeCode", ctypes.c_ushort),
        ("Flags", ctypes.c_ushort),
        ("Reserved", ctypes.c_ushort),
        ("VKey", ctypes.c_ushort),
        ("Message", ctypes.c_uint),
        ("ExtraInformation", ctypes.c_ulong)
    ]

class RAWINPUT(ctypes.Structure):
    class _u(ctypes.Union):
        _fields_ = [("keyboard", RAWKEYBOARD)]
    _anonymous_ = ("u",)
    _fields_ = [
        ("header", RAWINPUTHEADER),
        ("u", _u)
    ]

# WNDPROC 타입 선언 (윈도우 프로시저 콜백)
WNDPROC = ctypes.WINFUNCTYPE(LRESULT, wintypes.HWND, wintypes.UINT, wintypes.WPARAM, wintypes.LPARAM)

# 가상 키 코드 -> 문자열 매핑 딕셔너리
VK_MAPPING = {
    0x30: "0",   0x31: "1",   0x32: "2",   0x33: "3",   0x34: "4",
    0x35: "5",   0x36: "6",   0x37: "7",   0x38: "8",   0x39: "9",
    0x41: "A",   0x42: "B",   0x43: "C",   0x44: "D",   0x45: "E",
    0x46: "F",   0x47: "G",   0x48: "H",   0x49: "I",   0x4A: "J",
    0x4B: "K",   0x4C: "L",   0x4D: "M",   0x4E: "N",   0x4F: "O",
    0x50: "P",   0x51: "Q",   0x52: "R",   0x53: "S",   0x54: "T",
    0x55: "U",   0x56: "V",   0x57: "W",   0x58: "X",   0x59: "Y",
    0x5A: "Z",
    0x20: "SPACE",
    0x0D: "ENTER",
    0x1B: "ESC",
    0x09: "TAB",
    0x08: "BACK",
    0x70: "F1",  0x71: "F2",  0x72: "F3",  0x73: "F4",
    0x74: "F5",  0x75: "F6",  0x76: "F7",  0x77: "F8",
    0x78: "F9",  0x79: "F10", 0x7A: "F11", 0x7B: "F12",
    0x2D: "INS",
    0x2E: "DEL",
    0x25: "LEFT", 0x26: "UP", 0x27: "RIGHT", 0x28: "DOWN",
    0x14: "CAPS",
    0x90: "NUM\nLOCK",
    0x60: "NUM\n0",
    0x61: "NUM\n1", 0x62: "NUM\n2", 0x63: "NUM\n3",
    0x64: "NUM\n4", 0x65: "NUM\n5", 0x66: "NUM\n6",
    0x67: "NUM\n7", 0x68: "NUM\n8", 0x69: "NUM\n9",
    0x6A: "N *", 0x6B: "NUM\n+", 0x6C: "NUM\nENT",
    0x6D: "NUM\n-", 0x6E: "NUM\n.", 0x6F: "N /",
    0xFF: "PRT",
    0x2C: "PRT",
    0xBB: "=",
    0xBD: "-",
    0xC0: "`",
    0xDB: "[",
    0xDD: "]",
    0xDC: "\\",
    0xBA: ";",
    0xDE: "'",
    0xBC: ",",
    0xBE: ".",
    0xBF: "/",
    0xA0: "LSHIFT",
    0xA1: "RSHIFT",
    0x11: "CTRL",
    0x5B: "WIN",
    0x12: "ALT",
    0x15: "한/영",
    0x19: "한자",
    0x10: "SHIFT"
}


# exe 빌드 시 파일 경를 찾기 위한 함수
def resource_path(relative_path):
    try:
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")
    return os.path.join(base_path, relative_path)


# ===============================
# Raw Input 관련 유틸리티 함수
# ===============================
def get_device_name(hDevice: int) -> str:
    """
    주어진 hDevice 핸들을 통해 장치 이름을 반환합니다.
    """
    size = ctypes.c_uint(0)
    if user32.GetRawInputDeviceInfoW(hDevice, RIDI_DEVICENAME, None, ctypes.byref(size)) == 0:
        buffer = ctypes.create_unicode_buffer(size.value)
        if user32.GetRawInputDeviceInfoW(hDevice, RIDI_DEVICENAME, buffer, ctypes.byref(size)) > 0:
            return buffer.value
    return None

def register_raw_input(hwnd: int) -> None:
    """
    지정된 윈도우 핸들에 대해 Raw Input을 등록합니다.
    legacy 메시지(WM_KEYDOWN 등)를 생성하지 않도록 설정합니다.
    """
    rid = RAWINPUTDEVICE()
    rid.usUsagePage = 0x01   # Generic Desktop Controls
    rid.usUsage = 0x06       # Keyboard
    rid.dwFlags = RIDEV_INPUTSINK | RIDEV_NOLEGACY
    rid.hwndTarget = hwnd
    if not user32.RegisterRawInputDevices(ctypes.byref(rid), 1, ctypes.sizeof(rid)):
        raise ctypes.WinError()

def unregister_raw_input() -> None:
    """
    등록된 Raw Input을 해제합니다.
    """
    rid = RAWINPUTDEVICE()
    rid.usUsagePage = 0x01
    rid.usUsage = 0x06
    rid.dwFlags = RIDEV_REMOVE
    rid.hwndTarget = 0
    if not user32.RegisterRawInputDevices(ctypes.byref(rid), 1, ctypes.sizeof(rid)):
        raise ctypes.WinError()
    
# 디버깅 로그를 파일에 기록하기 위한 설정
def setup_debugging_log():
    """
    디버깅 로그를 파일에 기록하기 위한 설정을 합니다.
    """
    downloads_path = os.path.join(os.path.expanduser("~"), "Downloads")
    log_file_path = os.path.join(downloads_path, "example.txt")
    logging.basicConfig(
        filename=log_file_path,
        level=logging.DEBUG,
        format="%(asctime)s - %(levelname)s - %(message)s",
        filemode="w",  # 파일 모드를 'w'로 설정하여 덮어쓰기
    )
    logging.debug("디버깅 로그 시작")

# ===============================
# TestApp 클래스 정의 (메인 GUI 애플리케이션)
# ===============================
class TestApp(ttkb.Window):
    def __init__(self):

        setup_debugging_log()  # 디버깅 로그 설정
        super().__init__(themename="flatly")
        self.title("KkomDae Diagnostics")
        self.geometry("1700x950")
        self.resizable(False, False)
        self._style = ttkb.Style()

        # 변수 및 상태 초기화
        self._init_variables()

        # random key 
        self.random_key = None

        # UI 구성
        self.create_title_section()
        self.create_test_items()

        self.validate_random_key()

    def validate_random_key(self):
        """
        랜덤키 검증을 위한 메서드입니다.
        """
        # 랜덤키를 입력받습니다.
        key_window = ttkb.Toplevel(self)
        key_window.title("랜덤키 입력")
        key_window.geometry("500x300")
        key_window.resizable(False, False)

        # 모달로 설정하여 부모 창과의 상호작용을 막음
        key_window.grab_set()
        key_window.transient(self)
        
        def on_close():
            """
            창을 닫을 때 전체 애플리케이션 종료
            """
            if messagebox.askokcancel("종료", "애플리케이션을 종료하시겠습니까?"):
                key_window.grab_release()  # grab 해제
                key_window.destroy()
                self.quit()  # 애플리케이션 종료

        # 키 입력 레이블 및 입력 필드 생성
        key_label = ttkb.Label(key_window,
                               text='앱에서 발급받은 키를 입력하세요',
                               font=("Arial", 12))
        key_label.pack(pady=10)
        
        # 키 입력 필드
        key_var = ttkb.StringVar()
        key_entry = ttkb.Entry(key_window, textvariable=key_var, font=("Arial", 12))
        key_entry.pack(pady=10)
        
        def on_submit():
            """
            키 입력 후 확인 버튼 클릭 시 호출되는 메서드입니다.
            """
            key = key_var.get()
            vaild = self.check_random_key(key)
            print(f'입력한 키: {key}')
            print(f'유효성 검사 결과: {vaild}')
            if vaild:
                self.random_key = key
                key_window.grab_release()  # grab 해제
                key_window.destroy()
            else:
                messagebox.showerror("오류", "유효하지 않은 키입니다.")
        
        # 확인 버튼 생성
        submit_button = ttkb.Button(key_window, 
                                    text="확인", 
                                    command=on_submit,
                                    bootstyle="primary")
        submit_button.pack(pady=10)

        # ESC 키 등으로 창을 강제로 닫지 못하도록 함
        key_window.protocol("WM_DELETE_WINDOW", on_close)
        
    def check_random_key(self, key: str) -> bool:
        """
        서버에 랜덤키 확인 요청
        """
        try:
            # 테스트환경
            url = "http://localhost:8080/api/verify-key"
            # 운영환경
            # url = "https://j12d101.p.ssafy.io/api/verify-key"

            response = requests.get(url, params={"key": key})
            # 서버 응답 확인
            if response.status_code == 200:
                result = response.json().get("data")
                print(result)
                TorF = result.get("isValid")
                print(f'TorF : {TorF}')
                if TorF == 'true':
                    print("유효한 키입니다.")
                    return True
                else:
                    print("유효하지 않은 키입니다.")
                    return False
            else:
                logging.error(f"서버 오류: {response.status_code}")
                return False
        except requests.RequestException as e:
            logging.error(f"요청 오류: {e}")
            return False


    def _init_variables(self) -> None:
        """
        내부 변수와 상태를 초기화합니다.
        """
        # 내부 키보드의 Raw Input device 화이트리스트
        self.INTERNAL_HWIDS = ["\\ACPI#MSF0001"]

        # 테스트 완료 여부 딕셔너리
        self.test_done = {
            "키보드": False,
            "카메라": False,
            "USB": False,
            "충전": False,
            "배터리": False,
            "QR코드": False
        }

        # 테스트 상태 문자열 설정
        self.test_status = {
            "키보드": "테스트 전",
            "카메라": "테스트 전",
            "충전": "테스트 전",
            "배터리": "생성 전",
            "QR코드": "생성 전"
        }
        self.test_status_ing = {
            "키보드": "테스트 중",
            "카메라": "테스트 중",
            "충전": "테스트 중",
            "배터리": "생성 중",
            "QR코드": "생성 중"
        }

        # 테스트 상태 라벨 저장 딕셔너리
        self.test_status_labels = {}

        # 열려있는 테스트 창 관리 딕셔너리
        self.active_test_windows = {}

        # qr코드를 생성하기 위해 관리하는 set
        self.test_list = {"키보드", "USB", "카메라", "충전", "배터리"}
        # 폰트 경로 설정
        self.samsung_bold_path = resource_path("resource/font/SamsungSharpSans-Bold.ttf")
        self.samsung_regular_path = resource_path("resource/font/SamsungOne-400.ttf")
        self.samsung_700_path = resource_path("resource/font/SamsungOne-700.ttf")
        self.notosans_path = resource_path("resource/font/NotoSansKR-VariableFont_wght.ttf")

        # 각 테스트 상태에 따른 이미지 생성
        self.status_images = {
            "테스트 전": self.create_text_image("테스트 전", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=True),
            "테스트 중": self.create_text_image("테스트 중", (120, 30), self.notosans_path, 16, (255, 165, 0), align_left=True),
            "테스트 완료": self.create_text_image("테스트 완료", (120, 30), self.notosans_path, 16, (0, 128, 0), align_left=True),
            "생성 전": self.create_text_image("생성 전", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=True),
            "생성 중": self.create_text_image("생성 중", (120, 30), self.notosans_path, 16, (255, 165, 0), align_left=True),
            "생성 완료": self.create_text_image("생성 완료", (120, 30), self.notosans_path, 16, (0, 128, 0), align_left=True),
            "오류 발생": self.create_text_image("오류 발생", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=True),
            "USB테스트 전":{
                1:self.create_text_image("① 연결 필요", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=False),
                2:self.create_text_image("② 연결 필요", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=False),
                3:self.create_text_image("③ 연결 필요", (120, 30), self.notosans_path, 16, (255, 0, 0), align_left=False)
            },
            "USB테스트 완료":{
                1:self.create_text_image("① 연결 확인", (120, 30), self.notosans_path, 16, (0, 128, 0), align_left=True),
                2:self.create_text_image("② 연결 확인", (120, 30), self.notosans_path, 16, (0, 128, 0), align_left=True),
                3:self.create_text_image("③ 연결 확인", (120, 30), self.notosans_path, 16, (0, 128, 0), align_left=True)
            },
        }
        # 버튼 이미지 생성
        self.button_images = {
            "누르지 못한 키 보기": {
                "normal": self.create_text_image("누르지 못한 키 보기", (200, 30), self.notosans_path, 16, (255, 255, 255)),
                "disabled": self.create_text_image("누르지 못한 키 보기", (200, 30), self.notosans_path, 16, (180, 180, 180)),
            },
            "새로고침": {
                "normal": self.create_text_image("새로고침", (200, 30), self.notosans_path, 16, (255, 255, 255)),
                "disabled": self.create_text_image("새로고침", (200, 30), self.notosans_path, 16, (180, 180, 180)),
            },
            "리포트 확인하기": {
                "normal": self.create_text_image("리포트 확인하기", (200, 30), self.notosans_path, 16, (255, 255, 255)),
                "disabled": self.create_text_image("리포트 확인하기", (200, 20), self.notosans_path, 16, (180, 180, 180)),
            },
        }

        # resource_path 함수를 이용해 이미지 파일의 경로를 동적으로 설정
        self.test_icons = {
            "키보드": resource_path("resource/image/keyboard.png"),
            "카메라": resource_path("resource/image/camera.png"),
            "USB": resource_path("resource/image/usb.png"),
            "충전": resource_path("resource/image/charging.png"),
            "배터리": resource_path("resource/image/battery.png"),
            "QR코드": resource_path("resource/image/qrcode.png")
        }

        self.test_descriptions = {
            "키보드": "키 입력이 정상적으로 작동하는지 확인합니다.",
            "카메라": "카메라(웹캠)가 정상적으로 작동하는지 확인합니다.",
            "USB": "모든 USB 포트가 정상적으로 인식되는지 확인합니다.",
            "충전": "노트북이 정상적으로 충전되는지 확인합니다.",
            "배터리": "배터리 리포트를 생성하여 성능을 확인합니다.",
            "QR코드": "테스트 결과를 QR 코드로 생성합니다."
        }

        # USB 관련 변수 초기화
        self.usb_ports = self.get_all_usb_ports()
        self.usb_test_complete = False

        # 배터리 리포트 파일 경로 초기화
        self.report_path = None
        self.report = None

        # 키보드 테스트 관련 변수
        self.failed_keys = []
        self.pressed_keys = set()
        self.keys_not_pressed = set()
        self.all_keys = set()
        self.key_widgets = {}
        self.last_key_time = {}
        self.keyboard_layout = [
            [("ESC", 6), ("F1", 6), ("F2", 6), ("F3", 6), ("F4", 6), ("F5", 6),
            ("F6", 6), ("F7", 6), ("F8", 6), ("F9", 6), ("F10", 6), ("F11", 6),
            ("F12", 6), ("PRT", 6), ("INS", 6), ("DEL", 6), ("N /", 6), ("N *", 6)],

            [("`", 6), ("1", 7), ("2", 7), ("3", 7), ("4", 7), ("5", 7),
            ("6", 7), ("7", 7), ("8", 7), ("9", 7), ("0", 7), ("-", 7),
            ("=", 7), ("BACK", 8), ("NUM\n-", 6), ("NUM\n+", 6), ("NUM\nLOCK", 6)],

            [("TAB", 8), ("Q", 7), ("W", 7), ("E", 7), ("R", 7), ("T", 7),
            ("Y", 7), ("U", 7), ("I", 7), ("O", 7), ("P", 7), ("[", 7),
            ("]", 7), ("\\", 6), ("NUM\n7", 6), ("NUM\n8", 6), ("NUM\n9", 6)],

            [("CAPS", 11), ("A", 7), ("S", 7), ("D", 7), ("F", 7), ("G", 7),
            ("H", 7), ("J", 7), ("K", 7), ("L", 7), (";", 7), ("'", 7),
            ("ENTER", 11), ("NUM\n4", 6), ("NUM\n5", 6), ("NUM\n6", 6)],

            [("LSHIFT", 15), ("Z", 7), ("X", 7), ("C", 7), ("V", 7), ("B", 7),
            ("N", 7), ("M", 7), (",", 7), (".", 7), ("/", 7), ("RSHIFT", 15),
            ("NUM\n1", 6), ("NUM\n2", 6), ("NUM\n3", 6)],

            [("CTRL", 8), ("", 7), ("WIN", 7), ("ALT", 7), ("SPACE", 31), ("한/영", 7),
            ("한자", 7), ("LEFT", 7), ("DOWN", 7), ("UP", 7), ("RIGHT", 7),
            ("NUM\n0", 6), ("NUM\n.", 6), ("NUM\nENT", 6)]
        ]
    # -------------------------------
    # UI 구성 메서드들
    # -------------------------------
        # 🔹 Frame 스타일 설정
        self._style.configure("Blue.TFrame", background="#0078D7")   # 타이틀 배경 파란색
        self._style.configure("White.TFrame", background="white")   # 테스트 영역 배경 흰색


    def create_text_image(self, text: str, size: tuple, font_path: str, font_size: int, color: tuple, align_left: bool = False) -> ImageTk.PhotoImage:
        """
        텍스트를 이미지로 변환하여 반환합니다.
        """
        img = Image.new("RGBA", size, (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        try:
            font = ImageFont.truetype(font_path, font_size)
        except IOError:
            font = ImageFont.load_default()

        # 텍스트 위치 계산
        text_bbox = draw.textbbox((0, 0), text, font=font)
        text_x = 10 if align_left else (size[0] - text_bbox[2]) // 2
        text_y = (size[1] - font_size) // 2
        draw.text((text_x, text_y), text, font=font, fill=color, spacing=2, stroke_width=0.2)
        return ImageTk.PhotoImage(img)

    # 키 이미지를 생성하는 헬퍼 함수
    def create_key_image(self, key: str, size: tuple, pressed: bool = False) -> ImageTk.PhotoImage:
        """
        주어진 키 문자열과 크기에 따라, 상태(pressed 여부)에 따라 다른 배경/텍스트 색상의 이미지를 생성합니다.
        """
        # 상태에 따라 배경 및 텍스트 색상 설정
        if not pressed:
            bg_color = (220, 220, 220)   # 일반 상태: 연한 회색
            text_color = (0, 0, 0)         # 검은색 텍스트
        else:
            bg_color = (100, 100, 100)     # pressed 상태: 어두운 회색
            text_color = (255, 255, 255)   # 흰색 텍스트

        # 지정된 크기로 배경색이 채워진 이미지 생성 (RGBA 모드)
        img = Image.new("RGBA", size, bg_color + (255,))
        draw = ImageDraw.Draw(img)
        try:
            font = ImageFont.truetype(self.notosans_path, 16)
        except IOError:
            font = ImageFont.load_default()

        # 텍스트 크기 측정 후 중앙 정렬
        text_bbox = draw.textbbox((0, 0), key, font=font, align='center')
        text_width = text_bbox[2] - text_bbox[0]
        text_height = text_bbox[3] - text_bbox[1]
        text_x = (size[0] - text_width) // 2
        text_y = ((size[1] - text_height) // 2) - 6
        draw.text((text_x, text_y), key, font=font, fill=text_color, align='center')
        return ImageTk.PhotoImage(img)

    def update_status(self, test_name, new_status):
        """
        테스트 상태를 업데이트합니다.
        """
        self.detail = None
        if new_status in ["테스트 완료", "생성 완료"]:
            if test_name == "배터리":
                self.detail = self.report
                print(self.detail)
            self.send_test_result(test_name, True, self.detail)
        elif new_status == "오류 발생":
            if test_name == "USB":
                self.detail = [port for port, connected in self.usb_ports.items() if not connected]
            elif test_name == "키보드":
                self.detail = sorted(self.failed_keys)
            else:
                self.detail = None
            print(self.detail)
            self.send_test_result(test_name, False, self.detail)

        # 테스트 결과 이미지 변경
        if test_name in ["키보드", "카메라", "충전", "배터리"]:
            status_label = self.test_status_labels[test_name]
            new_img = self.status_images[new_status]
            status_label.config(image=new_img)
            status_label.image = new_img  # 이미지 참조 유지

    def create_title_section(self) -> None:
        """
        상단 타이틀 영역을 생성합니다.
        """
        title_frame = ttkb.Frame(self, style="Blue.TFrame")
        title_frame.place(relx=0, rely=0, relwidth=1, relheight=0.27)

        # SSAFY 로고 이미지 삽입
        img_path = resource_path("resource/image/ssafy_logo.png")
        image = Image.open(img_path).resize((80, 60), Image.LANCZOS)
        self.ssafy_logo = ImageTk.PhotoImage(image)
        img_label = ttkb.Label(title_frame, image=self.ssafy_logo, background="#0078D7", anchor="w")
        img_label.grid(row=0, column=0, padx=30, pady=(30, 10), sticky="w")

        # 타이틀 및 서브타이틀 텍스트 이미지 생성
        text_container = ttkb.Frame(title_frame, style="Blue.TFrame")
        text_container.grid(row=1, column=0, padx=20, sticky="w")

        self.title_img = self.create_text_image(
            "KkomDae Diagnostics", (800, 55), self.samsung_regular_path, 40, (255, 255, 255), align_left=True
        )
        title_label = ttkb.Label(text_container, image=self.title_img, background="#0078D7", anchor="w")
        title_label.grid(row=0, column=0, sticky="w")

        self.subtitle_img1 = self.create_text_image(
            "KkomDae Diagnostics로 노트북을 빠르고 꼼꼼하게 검사해보세요.",
            (800, 45), self.notosans_path, 20, (255, 255, 255, 255), align_left=True
        )
        subtitle_label1 = ttkb.Label(text_container, image=self.subtitle_img1, background="#0078D7", anchor="w")
        subtitle_label1.grid(row=1, column=0, sticky="w")

        self.subtitle_img2 = self.create_text_image(
            "각 테스트 항목의 아이콘을 클릭하면 테스트 또는 결과를 생성할 수 있습니다.",
            (800, 30), self.notosans_path, 17, (255, 255, 255, 255), align_left=True
        )
        subtitle_label2 = ttkb.Label(text_container, image=self.subtitle_img2, background="#0078D7", anchor="w")
        subtitle_label2.grid(row=2, column=0, sticky="w")

    def create_test_items(self) -> None:
        """
        각 테스트 항목(키보드, 카메라, USB, 충전, 배터리, QR코드)의 UI를 생성합니다.
        2행 3열의 격자 배치로 구성합니다.
        """
        test_frame = ttkb.Frame(self, style="White.TFrame")
        test_frame.place(relx=0.1, rely=0.35, relwidth=0.8, relheight=0.6)
        self.tests = ["키보드", "카메라", "USB", "충전", "배터리", "QR코드"]

        # 2행으로 균등하게 분배 (각 행의 최소 높이 200)
        for row in range(2):
            test_frame.grid_rowconfigure(row, weight=1, minsize=200)
        # 3열로 균등하게 분배 (각 열의 최소 폭 250)
        for col in range(3):
            test_frame.grid_columnconfigure(col, weight=1, minsize=250) # minsize를 250으로 늘려줌

        # 각 테스트 항목을 2행 3열의 격자에 배치합니다.
        for idx, name in enumerate(self.tests):
            row = idx // 3  # 0,1,2 -> 0 / 3,4,5 -> 1
            col = idx % 3   # 0,3 -> 0 / 1,4 -> 1 / 2,5 -> 2
            self.create_test_item(test_frame, name, row, col)

    def create_test_item(self, parent, name: str, row: int, col: int) -> None:
        """
        각 테스트 항목의 UI를 생성하고, 격자에 배치합니다.
        """
        # 컨테이너 프레임을 고정 크기로 생성 (크기는 원하는 대로 조정)
        frame = ttkb.Frame(parent, padding=10, width=250, height=200) # width를 250으로 수정
        frame.grid(row=row, column=col, padx=10, pady=10, sticky="nsew") # sticky 옵션 추가로 전체 격자 채우기

        # [Row 0] 아이콘 전용 프레임 (고정 크기, 최상단에 배치)
        icon_frame = ttkb.Frame(frame, width=55, height=55)
        icon_frame.grid(row=0, column=0,sticky= "n", pady=(0, 5), padx=10)

        # 아이콘 이미지 로드 및 명암(채도) 낮추기
        icon_path = self.test_icons.get(name, "default.png")
        icon_img = Image.open(icon_path).resize((50, 50), Image.LANCZOS)
        enhancer = ImageEnhance.Color(icon_img)
        icon_img = enhancer.enhance(0)  # 채도를 0으로 낮춰 흑백 효과
        icon_photo = ImageTk.PhotoImage(icon_img)
        icon_label = ttkb.Label(icon_frame, image=icon_photo,justify='center')
        icon_label.image = icon_photo  # 이미지 참조 유지
        icon_label.pack(expand=True, fill="both") # grid 에서 pack으로 수정해줍니다.

        # 타이틀을 이미지로 변경
        title_img = self.create_text_image(
            text=name,
            size=(200, 30),  # 필요에 따라 사이즈 조절
            font_path=self.notosans_path,  # 적절한 폰트 지정
            font_size=20,
            color=(102, 102, 102),
            align_left=True,
        )
        title_label = ttkb.Label(frame, image=title_img)
        title_label.image = title_img
        title_label.grid(row=1, column=0, sticky="ew", pady=(5, 0))

        # 설명(subtitle)을 이미지로 변경
        description_img = self.create_text_image(
            text=self.test_descriptions.get(name, ""),
            size=(350, 60),  # 설명이 길면 height를 더 늘리기
            font_path=self.notosans_path,  # 적절한 폰트 지정
            font_size=15,
            color=(102, 102, 102),
            align_left=True,
        )
        desc_label = ttkb.Label(frame, image=description_img)
        desc_label.image = description_img
        desc_label.grid(row=2, column=0, sticky="ew", pady=(5, 0))

        # 테스트 상태를 이미지로 변경
        status_img = self.status_images[self.test_status.get(name, "테스트 전")]
        # 이미지를 라벨로 관리
        status_label = ttkb.Label(frame, image=status_img)
        status_label.image = status_img
        status_label.grid(row=3, column=0, sticky="ew", pady=(5, 0))
        self.test_status_labels[name] = status_label


        if name == "키보드":
            self.failed_keys_button = ttkb.Button(
                frame,
                image=self.button_images["누르지 못한 키 보기"]["disabled"],
                state="disabled",
                bootstyle=WARNING,
                command=self.show_failed_keys
            )
            self.failed_keys_button.grid(row=4, column=0, sticky="ew", pady=(5, 0))

        elif name == "USB":
            # USB의 경우 상태 레이블은 숨기고, 포트 상태와 새로고침 버튼을 별도의 행에 배치
            status_label.grid_forget()
            self.usb_status_label = status_label
            # USB 포트 상태 레이블들을 담을 프레임
            self.usb_ports_frame = ttkb.Frame(frame)
            self.usb_ports_frame.grid(row=3, column=0 )
            self.usb_ports_frame.grid_columnconfigure(0, weight=1)

            # 동적으로 포트 상태 표시 업데이트
            self.update_usb_port_display()

            # 새로고침 버튼 생성
            self.usb_refresh_button = ttkb.Button(
                frame,
                image=self.button_images["새로고침"]["disabled"],
                bootstyle=SECONDARY,
                command=self.refresh_usb_check,
                state="disabled"
            )
            self.usb_refresh_button.grid(row=4, column=0, sticky="ew", pady=(5, 0))

        elif name == "배터리":
            self.battery_report_button = ttkb.Button(
                frame,
                image=self.button_images["리포트 확인하기"]["normal"],
                bootstyle=SECONDARY,
                command=self.view_battery_report
            )
            self.battery_report_button.grid(row=4, column=0, sticky="ew", pady=(5, 0))

        # 항목 전체를 클릭하면 해당 테스트 시작 (아이콘 레이블 등에도 이벤트 바인딩)
        frame.bind("<Button-1>", lambda e: self.start_test(name))
        icon_label.bind("<Button-1>", lambda e: self.start_test(name))

    def get_all_usb_ports(self) -> dict:
        """
        시스템의 모든 USB 포트(숨겨진 포트 포함)를 검색하여 초기 상태를 설정합니다.
        반환값은 예시로 {'port1': 상태, 'port3': 상태} 형태로 출력됩니다.
        """
        usb_ports = {}
        try:
            cmd = (
                    'powershell.exe -WindowStyle Hidden -Command "'
                    '$OutputEncoding = [System.Text.UTF8Encoding]::new(); '
                    'Get-PnpDevice -Class USB -PresentOnly:$false | '
                    'Select-Object InstanceId | '
                    'ConvertTo-Json'
                    '"'
                )
            # CREATE_NO_WINDOW 플래그 추가하여 콘솔 창 숨기기
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                encoding='cp949',
                errors='replace',
                startupinfo=startupinfo
            )
            logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 실행 결과 (숨겨진 장치 포함):")
            logging.debug(f"  - 반환 코드: {result.returncode}")
            logging.debug(f"  - 표준 출력: {result.stdout}")
            logging.debug(f"  - 표준 에러: {result.stderr}")
            
            if result.returncode == 0:
                try:
                    devices = json.loads(result.stdout)
                    logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 JSON 파싱 결과:")
                    logging.debug(f"  - 파싱된 데이터: {devices}")
                    if not devices:
                        logging.debug("디버깅: 첫 번째 PowerShell 명령어 결과 - USB 장치 없음")
                    else:
                        # 단일 장치인 경우 리스트로 변환
                        if isinstance(devices, dict):
                            devices = [devices]
                        
                        for device in devices:
                            logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 장치 정보 처리 시작: {device}")
                            if 'InstanceId' in device:
                                instance_id = device['InstanceId']
                                logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - InstanceId: {instance_id}")
                                # USB 장치인지 확인 (앞부분이 "USB\\"여야 함)
                                if instance_id.startswith("USB\\"):
                                    logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - USB 장치 확인: {instance_id}")
                                    # 정규 표현식으로 "&0&숫자" 패턴을 추출 (숫자는 한 자리 이상)
                                    match = re.search(r'&0&(\d)$', instance_id)
                                    if match:
                                        port_number = int(match.group(1))
                                        logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 포트 번호 추출: {port_number}")
                                        # 여기서 원하는 포트 번호만 처리 (예: 1, 2, 3번)
                                        if port_number in [1, 2, 3]:
                                            key = f'port{port_number}'
                                            logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 유효한 포트 번호: {key}")
                                            # 첫번째 명령어에서는 기본 상태 False
                                            if key not in usb_ports:
                                                usb_ports[key] = False
                                                logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 새로운 포트 추가: {key}, 상태: False")
                                                logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 현재 포트 상태: {usb_ports}")
                                                logging.debug(f'디버깅: 첫 번째 PowerShell 명령어 - divece: {device}')
                                        else:
                                            logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 처리하지 않는 포트 번호: {port_number}")
                                    else:
                                        logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 포트 번호 패턴 불일치: {instance_id}")
                                else:
                                    logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - USB 장치가 아님: {instance_id}")
                            else:
                                logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - InstanceId 키 없음: {device}")
                except json.JSONDecodeError as e:
                    logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - JSON 파싱 오류: {e}")
                    return usb_ports
            else:
                logging.debug(f"디버깅: 첫 번째 PowerShell 명령어 - 오류 발생")
            
            # 연결된 USB 장치 상태 확인
            cmd_connected = (
                'powershell.exe -WindowStyle Hidden -NonInteractive -Command "'
                '$OutputEncoding = [System.Text.UTF8Encoding]::new(); '
                'Get-PnpDevice -Class USB -PresentOnly:$true | '
                'Select-Object InstanceId | '
                'ConvertTo-Json'
                '"'
            )
            
            result_connected = subprocess.run(
                cmd_connected,
                capture_output=True,
                text=True,
                encoding='cp949',
                errors='replace',
                creationflags=subprocess.CREATE_NO_WINDOW,
                startupinfo=startupinfo
            )
            logging.debug(f"디버깅: 두 번째 PowerShell 명령어 실행 결과 (연결된 장치):")
            logging.debug(f"  - 반환 코드: {result_connected.returncode}")
            logging.debug(f"  - 표준 출력: {result_connected.stdout}")
            logging.debug(f"  - 표준 에러: {result_connected.stderr}")
            
            if result_connected.returncode == 0:
                try:
                    connected_devices = json.loads(result_connected.stdout)
                    logging.debug(f"디버깅: 두 번째 PowerShell 명령어 JSON 파싱 결과:")
                    logging.debug(f"  - 파싱된 데이터: {connected_devices}")
                    if isinstance(connected_devices, dict):
                        connected_devices = [connected_devices]
                    
                    for device in connected_devices:
                        logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 장치 정보 처리 시작: {device}")
                        if 'InstanceId' in device:
                            instance_id = device['InstanceId']
                            logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - InstanceId: {instance_id}")
                            if instance_id.startswith("USB\\"):
                                logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - USB 장치 확인: {instance_id}")
                                match = re.search(r'&0&(\d)$', instance_id)
                                if match:
                                    port_number = int(match.group(1))
                                    logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 포트 번호 추출: {port_number}")
                                    # 원하는 포트 번호만 처리
                                    if port_number in [1, 2, 3]:
                                        key = f'port{port_number}'
                                        logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 유효한 포트 번호: {key}")
                                        # 연결된 장치이면 상태를 True로 업데이트
                                        usb_ports[key] = True
                                        logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 포트 상태 업데이트: {key}, 상태: True")
                                        logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 현재 포트 상태: {usb_ports}")
                                    else:
                                        logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 처리하지 않는 포트 번호: {port_number}")
                                else:
                                    logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - 포트 번호 패턴 불일치: {instance_id}")
                            else:
                                logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - USB 장치가 아님: {instance_id}")
                        else:
                            logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - InstanceId 키 없음: {device}")
                except json.JSONDecodeError as e:
                    logging.debug(f"디버깅: 두 번째 PowerShell 명령어 - JSON 파싱 오류: {e}")
                    pass
                    
        except Exception as e:
            logging.debug(f"디버깅: 예외 발생: {e}")
            pass
        
        logging.debug(f"디버깅: 최종 USB 포트 상태: {usb_ports}")
        return usb_ports

    # -------------------------------
    # 테스트 시작 및 완료 처리 메서드
    # -------------------------------
    def start_test(self, name: str) -> None:
        """
        테스트 카드 클릭 시 해당 테스트 실행.
        """
        # 테스트 시작 시 상태 변환
        if name != 'USB':
            self.update_status(name, self.test_status_ing.get(name, ""))

        if name == "키보드":
            self.open_keyboard_test()
        elif name == "카메라":
            self.open_camera_test()
        elif name == "USB":
            self.start_usb_check()
        elif name == "충전":
            self.start_c_type_check()
        elif name == "배터리":
            self.generate_battery_report()
        elif name == "QR코드":
            self.generate_qr_code()

        if name in ["카메라", "USB", "충전", "배터리"] and name in self.test_list:
            self.test_list.remove(name)  # 테스트 완료 후 삭제

    def mark_test_complete(self, test_name: str) -> None:
        """
        특정 테스트 완료 후 상태 업데이트 및 모든 테스트 완료시 메시지 출력.
        """
        if test_name in self.test_done:
            self.test_done[test_name] = True
            if test_name in ["배터리", "QR코드"]:
                self.update_status(test_name, "생성 완료")
            else:
                self.update_status(test_name, "테스트 완료")
            # 모든 테스트가 완료되었는지 확인
            if all(self.test_done.values()):
                messagebox.showinfo("모든 테스트 완료", "모든 테스트를 완료했습니다.\n수고하셨습니다!")

    def open_test_window(self, test_name: str, create_window_func) -> ttkb.Toplevel:
        """
        이미 열려있는 테스트 창이 있는지 확인 후, 새 창을 생성합니다.
        """
        if test_name in self.active_test_windows:
            messagebox.showwarning("경고", f"{test_name} 테스트 창이 이미 열려 있습니다.")
            return
        window = create_window_func()
        self.active_test_windows[test_name] = test_name
        return window

    def on_test_window_close(self, test_name: str) -> None:
        """
        테스트 창 종료 시 관리 딕셔너리에서 제거합니다.
        """
        if test_name in self.active_test_windows:
            del self.active_test_windows[test_name]
        if test_name in self.test_list:
            self.test_list.remove(test_name)  # 테스트 완료 후 삭제

    # -------------------------------
    # 키보드 테스트 관련 메서드
    # -------------------------------

    def open_keyboard_test(self) -> None:
        """
        키보드 테스트 창을 열어 Raw Input 이벤트를 처리합니다.
        """
        # 키보드 테스트 창 생성 (create_keyboard_window 메서드 사용)
        kb_window = self.open_test_window("키보드", self.create_keyboard_window)
        if kb_window is None:
            return

        # 키보드 레이아웃 구성 및 키 위젯 초기화
        self.setup_keyboard_layout(kb_window)

        # Raw Input 등록 및 윈도우 프로시저 설정
        hwnd = kb_window.winfo_id()
        register_raw_input(hwnd)
        kb_window.protocol("WM_DELETE_WINDOW", self.on_close_keyboard_window)
        self.set_raw_input_proc(hwnd, kb_window)


    def create_keyboard_window(self) -> ttkb.Toplevel:
        """
        키보드 테스트 창(Toplevel)을 생성하는 메서드
        """
        kb_window = ttkb.Toplevel(self)
        kb_window.title("키보드 테스트")
        kb_window.geometry("1800x700")
        # 테스트 안내 레이블 추가
        info_label = ttkb.Label(kb_window, text="모든 키를 한 번씩 눌러보세요.\n완료 시 창이 닫힙니다.")
        info_label.pack(pady=5)
        return kb_window


    def setup_keyboard_layout(self, kb_window: ttkb.Toplevel) -> None:
        """
        키보드 레이아웃 UI를 이미지 위젯을 사용하여 구성합니다.
        """

        # 키보드 레이아웃을 감싸는 프레임 생성
        keyboard_frame = ttkb.Frame(kb_window, borderwidth=2, padding=5)
        keyboard_frame.pack(pady=5)

        # 각 행별로 키 위젯 생성
        for row_index, row_keys in enumerate(self.keyboard_layout):
            row_frame = ttkb.Frame(keyboard_frame)
            row_frame.pack(pady=5, fill='x')
            for key, width in row_keys:
                if key == "":  # 빈 키(스페이서)는 건너뜁니다.
                    spacer = ttkb.Label(row_frame, text="", width=width, padding=(2, 12))
                    spacer.pack(side='left', padx=3)
                    continue
                key_upper = key.upper()
                self.all_keys.add(key_upper)
                # 이미지 기반 키 위젯 생성 (높이는 예를 들어 60px로 고정)
                if row_index == 0:
                    btn = self.create_key_widget(row_frame, key, width, height=30)
                else:
                    btn = self.create_key_widget(row_frame, key, width, height=60)
                btn.pack(side='left', padx=3)
                self.key_widgets[key_upper] = btn

        # 종료 버튼 프레임 생성
        button_frame = ttkb.Frame(kb_window)
        button_frame.pack(pady=20)
        # 테스트 완료 버튼 생성
        end_test_button = ttkb.Button(
            button_frame,
            text="테스트 종료",
            bootstyle="danger",
            command=self.on_close_keyboard_window,
            width=20,
            padding=10
        )
        end_test_button.pack()

        # 아직 눌리지 않은 키 목록은 전체 키에서 이전에 눌린 키들을 제외한 집합으로 설정
        self.keys_not_pressed = self.all_keys - self.pressed_keys

        # 모든 키가 눌렸으면 테스트 완료 처리
        if not self.keys_not_pressed:
            unregister_raw_input()
            messagebox.showinfo("키보드 테스트", "키보드 테스트 완료")
            self.failed_keys_button.config(
                state="disabled",
                image=self.button_images["누르지 못한 키 보기"]["disabled"]
            )
            self.close_keyboard_window()
            self.mark_test_complete("키보드")

    def create_key_widget(self, parent, key: str, width_unit: int, height: int = 60):
        """
        각 키에 대한 이미지 위젯을 생성합니다.
        width_unit는 키보드 레이아웃에 정의된 단위값이며, 픽셀 단위의 실제 너비로 변환하여 사용합니다.
        """
        # 예: 1 단위당 10 픽셀로 환산 (필요시 조정)
        pixel_width = width_unit * 10
        size = (pixel_width, height)
        key_upper = key.upper()

        # normal, pressed 상태 이미지 생성
        normal_img = self.create_key_image(key, size, pressed=False)
        pressed_img = self.create_key_image(key, size, pressed=True)

        # 각 키의 이미지 정보를 딕셔너리에 저장 (추후 상태 업데이트에 사용)
        if not hasattr(self, "key_images"):
            self.key_images = {}
        self.key_images[key_upper] = {"normal": normal_img, "pressed": pressed_img}

        # normal 이미지로 위젯 생성
        if key in self.pressed_keys:
            widget = ttkb.Label(parent, image=pressed_img)
        else:
            widget = ttkb.Label(parent, image=normal_img)
        widget.image = normal_img  # 이미지 참조 유지
        return widget


    def set_raw_input_proc(self, hwnd, kb_window):
        """
        Raw Input 윈도우 프로시저를 설정하고 기존 프로시저를 저장합니다.
        """
        # 인스턴스 변수에 Raw Input 프로시저 저장
        self._raw_input_wnd_proc = WNDPROC(self.raw_input_wnd_proc)
        cb_func_ptr = ctypes.cast(self._raw_input_wnd_proc, ctypes.c_void_p).value
        cb_func_ptr = LONG_PTR(cb_func_ptr)
        old_proc = user32.SetWindowLongPtrW(hwnd, GWL_WNDPROC, cb_func_ptr)
        self._kb_old_wnd_proc = old_proc
        self._kb_hwnd = hwnd
        self.kb_window_ref = kb_window


    def raw_input_wnd_proc(self, hWnd, msg, wParam, lParam):
        """
        Raw Input 메시지를 처리하는 윈도우 프로시저입니다.
        """
        # 창 종료 처리
        if msg == WM_NCDESTROY:
            if self._kb_old_wnd_proc is not None:
                user32.SetWindowLongPtrW(hWnd, GWL_WNDPROC, self._kb_old_wnd_proc)
                self._kb_old_wnd_proc = None
            return 0

        if msg == WM_INPUT:
            try:
                logging.debug("raw_input_wnd_proc: WM_INPUT 메시지 처리 시작")
                size = ctypes.c_uint(0)
                # 입력 데이터 크기 확인
                if user32.GetRawInputData(lParam, RID_INPUT, None, ctypes.byref(size),
                                            ctypes.sizeof(RAWINPUTHEADER)) == 0:
                    buffer = ctypes.create_string_buffer(size.value)
                    if user32.GetRawInputData(lParam, RID_INPUT, buffer, ctypes.byref(size),
                                            ctypes.sizeof(RAWINPUTHEADER)) == size.value:
                        raw = ctypes.cast(buffer, ctypes.POINTER(RAWINPUT)).contents
                        if raw.header.dwType == RIM_TYPEKEYBOARD:
                            # Key Down 이벤트만 처리
                            if (raw.u.keyboard.Flags & RI_KEY_BREAK) == 0:
                                vkey = raw.u.keyboard.VKey
                                make_code = raw.u.keyboard.MakeCode
                                flags = raw.u.keyboard.Flags
                                current_time = time.time()

                                # 중복 이벤트 방지 (0.1초 이내)
                                if vkey in self.last_key_time and (current_time - self.last_key_time[vkey] < 0.1):
                                    return 0
                                self.last_key_time[vkey] = current_time

                                logging.debug(f"raw_input_wnd_proc: 키 입력 감지, vkey={vkey}")

                                # 키 심볼 결정 (별도 메서드 호출)
                                key_sym = self.get_key_symbol(raw, flags)
                                if key_sym:
                                    # 내부 장치 여부 판단
                                    device_name = get_device_name(raw.header.hDevice)
                                    is_internal = False
                                    if device_name:
                                        device_name_lower = device_name.lower().replace("\\", "#")
                                        is_internal = any(
                                            internal_id.lower().replace("\\", "#") in device_name_lower
                                            for internal_id in self.INTERNAL_HWIDS
                                        )
                                    if is_internal:
                                        self.on_raw_key(key_sym)
                                else:
                                    # 매핑되지 않은 키의 경우 임시 매핑 및 사용자 안내
                                    if vkey not in VK_MAPPING:
                                        temp_key_sym = f"NEW_KEY_0x{vkey:02X}"
                                        VK_MAPPING[vkey] = temp_key_sym
                                        messagebox.showinfo('키 추가', f"해당 키({temp_key_sym})가 추가되었습니다. 한번 더 눌러주세요.")
            except Exception as e:
                logging.error(f"raw_input_wnd_proc 에러: {e}")
            return 0

        # 창이 유효하지 않은 경우 처리
        if not user32.IsWindow(hWnd):
            return 0

        # 기존 윈도우 프로시저 호출
        if self._kb_old_wnd_proc:
            return user32.CallWindowProcW(self._kb_old_wnd_proc, hWnd, msg, wParam, lParam)
        else:
            return user32.DefWindowProcW(hWnd, msg, wParam, lParam)


    def get_key_symbol(self, raw, flag) -> str:
        """
        Raw 입력 데이터로부터 키 심볼을 결정합니다.
        """
        vkey = raw.u.keyboard.VKey
        if vkey in VK_MAPPING:
            if vkey == 0x0D:
                return "NUM\nENT" if (raw.u.keyboard.Flags & RI_KEY_E0) else "ENTER"
            elif vkey == 0x10:
                if raw.u.keyboard.MakeCode == 0x2A:
                    return "LSHIFT"
                elif raw.u.keyboard.MakeCode == 0x36:
                    return "RSHIFT"
                else:
                    return "SHIFT"
            elif vkey == 0x2E:
                return "DEL" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n."
            elif vkey == 0x2D:
                return "INS" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n0"
            elif vkey == 0x28:
                return "DOWN" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n2"
            elif vkey == 0x25:
                return "LEFT" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n4"
            elif vkey == 0x27:
                return "RIGHT" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n6"
            elif vkey == 0x26:
                return "UP" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n8"
            else:
                return VK_MAPPING[vkey]
        elif vkey == 0x23:
            return "DEL" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n1"
        elif vkey == 0x22:
            return "Pg Dn" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n3"
        elif vkey == 0x0C:
            return "middle" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n5"
        elif vkey == 0x24:
            return "Home" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n7"
        elif vkey == 0x21:
            return "Pg Up" if (raw.u.keyboard.Flags & RI_KEY_E0) else "NUM\n9"
        return None


    def on_close_keyboard_window(self) -> None:
        """
        키보드 창 종료 시 누르지 않은 키가 있으면 기록한 후 창을 닫습니다.
        """
        if self.keys_not_pressed:
            unregister_raw_input()
            self.failed_keys = list(self.keys_not_pressed)
            self.update_status("키보드", "오류 발생")
            self.failed_keys_button.config(
                state="normal",
                image=self.button_images["누르지 못한 키 보기"]["normal"]
            )
        self.close_keyboard_window()


    def close_keyboard_window(self) -> None:
        """
        키보드 테스트 종료 시 Raw Input 프로시저를 복원하고 창을 닫습니다.
        """
        if hasattr(self, '_kb_hwnd') and self._kb_hwnd and self._kb_old_wnd_proc is not None:
            user32.SetWindowLongPtrW(self._kb_hwnd, GWL_WNDPROC, self._kb_old_wnd_proc)
            self._kb_old_wnd_proc = None
        if hasattr(self, 'kb_window_ref'):
            self.kb_window_ref.destroy()
        self.on_test_window_close("키보드")


    def on_raw_key(self, key: str) -> None:
        """
        키 입력 이벤트 처리: 해당 키가 처음 눌리면 이미지 상태를 pressed로 변경하고,
        모든 키가 눌리면 테스트 완료 처리를 진행합니다.
        """
        key_upper = key.upper()
        # 이미 눌린 키라면 중복 처리하지 않습니다.
        if key_upper not in self.pressed_keys:
            # 눌린 키를 상태에 추가
            self.pressed_keys.add(key_upper)
            # 아직 누르지 않은 키 목록에서 제거
            if key_upper in self.keys_not_pressed:
                self.keys_not_pressed.remove(key_upper)
            widget = self.key_widgets.get(key_upper)
            if widget and key_upper in self.key_images:
                pressed_img = self.key_images[key_upper]["pressed"]
                widget.config(image=pressed_img)
                widget.image = pressed_img  # 이미지 참조 유지
            # 모든 키가 눌렸으면 테스트 완료 처리
            if not self.keys_not_pressed:
                unregister_raw_input()
                messagebox.showinfo("키보드 테스트", "키보드 테스트 완료")
                self.failed_keys_button.config(
                    state="disabled",
                    image=self.button_images["누르지 못한 키 보기"]["disabled"]
                )
                self.close_keyboard_window()
                self.mark_test_complete("키보드")

    def show_failed_keys(self) -> None:
        """
        누르지 못한 키와 눌린 키 상태를 실제 키보드 레이아웃과 같이 시각적으로 확인할 수 있는 창을 엽니다.
        """
        # 새 창(Toplevel) 생성 및 기본 설정
        keys_win = ttkb.Toplevel(self)
        keys_win.title("키보드 누름 상태")
        keys_win.geometry("1400x800")

        # 전체 키보드 레이아웃을 감싸는 프레임 생성
        keyboard_frame = ttkb.Frame(keys_win, borderwidth=2, padding=5)
        keyboard_frame.pack(pady=5)

        # 각 행(row)별로 레이아웃 구성
        for row_index, row_keys in enumerate(self.keyboard_layout):
            # 행을 감싸는 프레임 생성
            row_frame = ttkb.Frame(keyboard_frame)
            row_frame.pack(pady=5, fill='x')

            # 각 키에 대해 처리
            for key, width in row_keys:
                if key == "":  # 빈 키(스페이서)는 별도 처리
                    spacer = ttkb.Label(row_frame, text="", width=width, padding=(2, 12))
                    spacer.pack(side='left', padx=3)
                    continue

                key_upper = key.upper()  # 키 값 대문자 변환

                # 각 키의 이미지 선택
                # 눌리지 않은 키(self.failed_keys에 포함되어 있다면) -> normal 이미지
                # 눌린 키 -> pressed 이미지
                if hasattr(self, "failed_keys") and key_upper in self.failed_keys:
                    img = self.key_images[key_upper]["normal"]
                else:
                    img = self.key_images[key_upper]["pressed"]

                # 이미지 위젯(Label) 생성 및 배치
                label = ttkb.Label(row_frame, image=img)
                label.image = img  # 이미지 참조 유지
                label.pack(side='left', padx=3)

    # -------------------------------
    # USB 테스트 관련 메서드
    # -------------------------------
    def start_usb_check(self) -> None:
        """
        USB 테스트 초기화 후 상태 갱신 및 새로고침 버튼 활성화
        """
        # USB 테스트 완료 플래그 초기화
        self.usb_test_complete = False

        # 새로고침 버튼 활성화
        self.usb_refresh_button.config(
            state="normal",
            image=self.button_images["새로고침"]["normal"]
            )

        self.refresh_usb_check()

    def refresh_usb_check(self) -> None:
        """
        USB 연결 상태를 확인하여 self.usb_ports 딕셔너리를 갱신한 후,
        동적으로 USB 포트 표시를 업데이트합니다.
        모든 포트가 연결되면 테스트 완료 처리를 진행합니다.
        """
        try:
            cmd_connected = (
                'powershell.exe -WindowStyle Hidden -NonInteractive -Command "'
                '$OutputEncoding = [System.Text.UTF8Encoding]::new(); '
                'Get-PnpDevice -Class USB -PresentOnly:$true '
                '| Select-Object InstanceId, FriendlyName, Name '
                '| ConvertTo-Json'
                '"'
            )

            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE

            result_connected = subprocess.run(
                cmd_connected,
                capture_output=True,
                text=True,
                encoding='cp949',
                errors='replace',
                creationflags=subprocess.CREATE_NO_WINDOW,
                startupinfo=startupinfo
            )
            logging.debug(f"디버깅: 두 번째 PowerShell 명령어 실행 결과 (연결된 장치):")
            logging.debug(f"  - 반환 코드: {result_connected.returncode}")
            logging.debug(f"  - 표준 출력: {result_connected.stdout}")
            logging.debug(f"  - 표준 에러: {result_connected.stderr}")

            if result_connected.returncode == 0:
                try:
                    connected_devices = json.loads(result_connected.stdout)
                    logging.debug(f"디버깅: 두 번째 PowerShell 명령어 JSON 파싱 결과: {connected_devices}")

                    if connected_devices:
                        if isinstance(connected_devices, dict):
                            connected_devices = [connected_devices]
                        
                        for device in connected_devices:
                            instance_id = device.get("InstanceId", "")
                            friendly_name = device.get("FriendlyName", "")
                            name_field = device.get("Name", "")
                            logging.debug(f"디버깅(연결된): InstanceId={instance_id}, FriendlyName={friendly_name}, Name={name_field}")

                            if instance_id.startswith("USB\\"):
                                match = re.search(r'&0&(\d)$', instance_id)
                                if match:
                                    port_number = int(match.group(1))
                                    if port_number in [1, 2, 3]:
                                        key = f'port{port_number}'
                                        self.usb_ports[key] = True
                                        logging.debug(f"  → {key} 상태 True로 업데이트")
                    else:
                        logging.debug("디버깅: 두 번째 PowerShell 명령어 결과 - 연결된 Composite 없음")
                except json.JSONDecodeError as e:
                    logging.debug(f"두 번째 PowerShell JSON 파싱 오류: {e}")

            # 동적으로 USB 포트 표시를 업데이트
            self.update_usb_port_display()

            # 새로고침 버튼 상태 업데이트 (모든 포트가 연결되면 버튼 비활성화)
            if all(self.usb_ports.values()):
                self.usb_test_complete = True
                self.usb_refresh_button.config(state="disabled")
                self.mark_test_complete("USB")
                messagebox.showinfo("USB Test", "모든 USB 포트 테스트 완료!")
            else:
                # 일부 포트가 연결되지 않은 경우, 사용자에게 안내
                self.update_status("USB", "오류 발생")
                messagebox.showinfo("USB Test", "USB 연결 상태를 확인해주세요.")

        except Exception as e:
            messagebox.showerror("USB Error", f"USB 포트 확인 중 오류 발생:\n{e}")
     
    def update_usb_port_display(self) -> None:
        """
        self.usb_ports 딕셔너리(예: {"port1": True, "port3": False})를 기반으로
        USB 포트 상태를 동적으로 업데이트합니다.
        기존 위젯을 삭제한 후, 각 포트 번호에 따라 상태에 맞는 이미지를 새로 생성합니다.
        """
        # 기존의 포트 상태 위젯 모두 삭제
        for widget in self.usb_ports_frame.winfo_children():
            widget.destroy()
        # USB 포트 딕셔너리를 정렬하여 왼쪽부터 순서대로 배치 (예: port1, port3)
        for key in sorted(self.usb_ports.keys(), key=lambda x: int(x.replace("port", ""))):
            status = self.usb_ports[key]  # True면 연결된 상태, False면 미연결
            port_num = int(key.replace("port", ""))
            # 상태에 따라 이미지를 선택합니다.
            if status:
                img = self.status_images["USB테스트 완료"][port_num]
            else:
                img = self.status_images["USB테스트 전"][port_num]
            # 각 포트를 담을 프레임 생성 후 포트 레이블 배치
            port_frame = ttkb.Frame(self.usb_ports_frame)
            port_frame.pack(side="left", padx=5, expand=True, fill="both")
            port_label = ttkb.Label(port_frame, image=img)
            port_label.image = img  # 이미지 참조 유지
            port_label.pack(expand=True, fill="both")
    # -------------------------------
    # 카메라 테스트 관련 메서드
    # -------------------------------

        self.camera_test_running = False
        self.camera_closing = False
        self.cap = None
        self.camera_update_after_id = None
        self.window_name = None
        self.photo_flag = True

    def open_camera_test(self) -> None:
        """
        카메라(웹캠) 테스트 창을 열어 프레임을 표시합니다.
        """
        # 카메라 flage 설정
        # 이미 카메라 테스트가 실행 중인지 확인
        if getattr(self, "camera_test_running", False) or getattr(self, "cap", None) is not None:
            messagebox.showinfo("정보", "카메라 테스트가 이미 실행 중입니다.")
            return

        # 진행 중인 종료 절차가 있는지(혹은 바로 전에 종료되었는지) 확인
        if getattr(self, "camera_closing", False):
            messagebox.showinfo("정보", "카메라 종료 처리가 끝날 때까지 기다려 주세요.")
            return

        # 테스트 시작 플래그 설정
        self.camera_test_running = True
        self.camera_closing = False

        try:
            # 기본 카메라(인덱스 0)를 CAP_DSHOW 옵션으로 열기
            self.cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
            self.window_name = "Camera Test - ESC to exit"
            cv2.namedWindow(self.window_name)
            # 카메라 프레임 업데이트 시작
            self.update_camera_frame()

        except Exception as e:
            messagebox.showerror("카메라 오류", f"카메라를 열 수 없습니다: {str(e)}")
            self.photo_flag = False
            self.close_camera_test()

    def update_camera_frame(self) -> None:
        """
        Tkinter의 after()를 이용하여 주기적으로 카메라 프레임을 업데이트합니다.
        """
        if not self.camera_test_running:
            return

        try:
            # 카메라에서 프레임 읽기
            ret, frame = self.cap.read()
            if not ret or frame is None:
                raise Exception("카메라 프레임을 읽을 수 없습니다.")

            # 읽은 프레임을 테스트 창에 표시
            cv2.imshow(self.window_name, frame)

            # 키 입력 감지 (ESC 키 또는 창 닫힘 시 테스트 종료)
            key = cv2.waitKey(1) & 0xFF
            if key == 27:  # ESC
                self.close_camera_test()
                return

            # OpenCV 창이 닫혔는지 검사
            if cv2.getWindowProperty(self.window_name, cv2.WND_PROP_VISIBLE) < 1:
                self.close_camera_test()
                return

            # 다음 프레임 업데이트 예약 (카메라가 실행 중일 때만)
            if self.camera_test_running:
                # 10ms 후 update_camera_frame 재호출
                self.camera_update_after_id = self.after(5, self.update_camera_frame)

        except Exception as e:
            messagebox.showerror("카메라 오류", f"예외 발생: {str(e)}")
            self.photo_flag = False
            self.close_camera_test()

    def close_camera_test(self) -> None:
        """
        카메라 테스트 종료 후 자원 해제 및 상태 복원.
        """
        # 이미 종료 절차가 진행 중인 경우
        if getattr(self, "camera_closing", False):
            return  # 두 번 처리 방지

        # 종료 절차 시작
        self.camera_closing = True

        # 테스트 중지 플래그 설정
        self.camera_test_running = False

        # after 콜백 취소
        if hasattr(self, "camera_update_after_id") and self.camera_update_after_id is not None:
            try:
                self.after_cancel(self.camera_update_after_id)
            except Exception as ex_cancel:
                pass
            self.camera_update_after_id = None

        # 카메라 자원 해제
        if hasattr(self, "cap") and self.cap is not None:
            self.cap.release()
            self.cap = None

        # OpenCV 창 닫기
        try:
            cv2.destroyWindow(self.window_name)
        except Exception as ex_destroy:
            cv2.destroyAllWindows()

        # 종료 절차 끝
        self.camera_closing = False

        if self.photo_flag:
            self.mark_test_complete("카메라")
        else:
            self.update_status("카메라", "오류 발생")
            self.photo_flag = True


    # -------------------------------
    # 충전 테스트 관련 메서드
    # -------------------------------
    def start_c_type_check(self) -> None:
        """
        충전 테스트를 시작하고 충전 포트 상태를 확인합니다.
        """
        self.c_type_ports = {"충전": False}
        # self.update_status("충전", "테스트 중")
        self.check_c_type_port()

    def check_c_type_port(self) -> None:
        """
        배터리 충전 상태를 확인하여 포트 상태를 갱신합니다.
        """
        battery = psutil.sensors_battery()
        if battery is None:
            messagebox.showerror("충전 Error", "배터리 정보를 가져올 수 없습니다.")
            self.update_status("충전", "오류 발생")
            return
        if not battery.power_plugged:
            messagebox.showinfo("충전 Test", "충전기가 연결되지 않았습니다.\n해당 포트에 충전기를 연결 후 다시 확인하세요.")
            self.update_status("충전", "오류 발생")
            return
        if not self.c_type_ports["충전"]:
            self.c_type_ports["충전"] = True
            messagebox.showinfo("충전 Test", "충전 확인되었습니다.")
            self.mark_test_complete("충전")

    # -------------------------------
    # 배터리 리포트 관련 메서드
    # -------------------------------
    def generate_battery_report(self) -> None:
        """
        powercfg 명령어를 통해 배터리 리포트를 생성합니다.
        """
        # 리포트 생성 작업을 별도의 스레드에서 실행
        threading.Thread(target=self._generate_battery_report_thread).start()

        # 사용자에게 진행 중임을 알리는 메시지 표시
        self.update_status("배터리", "생성 중")
    
    def _generate_battery_report_thread(self) -> None:
        """
        실제로 배터리 리포트를 생성하는 작업을 수행하는 메서드 (별도의 스레드에서 실행)
        """
        try:
            # 다운로드 폴더 경로를 가져옵니다.
            downloads_path = os.path.join(os.path.expanduser("~"), "Downloads")

            # 다운로드 폴더가 없는 경우, 생성합니다.
            if not os.path.exists(downloads_path):
                os.makedirs(downloads_path)

            # 컴퓨터 이름 가져오기
            computer_name = os.getenv("COMPUTERNAME")
            if computer_name is None:
                computer_name = "Unknown"
            # 현재 시간 가져오기
            now = datetime.now().strftime("%Y%m%d_%H%M%S")

            # 배터리 리포트 파일명 생성
            new_report_name = f"battery_report_{computer_name}_{now}.html"
            new_report_path = os.path.join(downloads_path, new_report_name)
            self.report = new_report_name

            # 리포트 생성
            temp_report_path = os.path.join(downloads_path, "battery_report.html")
            
            result = subprocess.run(
                ["powercfg", "/batteryreport", "/output", temp_report_path],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                check=False,  # check=False로 변경
                encoding='cp949',  # UTF-8 인코딩으로 변경
            )
        
            if result.returncode != 0:
                raise subprocess.CalledProcessError(result.returncode, result.args, result.stdout, result.stderr)

            # 리포트 파일 이름 변경
            os.rename(temp_report_path, new_report_path)

            self.report_path = new_report_path
            # GUI 업데이트는 메인 스레드에서 실행해야 함
            self.after(0, self._on_battery_report_generated)
        except subprocess.CalledProcessError as e:
            self.after(0, lambda: self._on_battery_report_error(f"명령 실행 중 오류 발생:\n{e.stderr}"))
        except Exception as e:
            self.after(0, lambda: self._on_battery_report_error(f"오류 발생:\n{e}"))


    def _on_battery_report_generated(self) -> None:
        """
        배터리 리포트 생성 완료 후 실행되는 콜백 메서드 (메인 스레드에서 실행)
        """
        messagebox.showinfo("배터리 리포트", f"배터리 리포트가 생성되었습니다.\n파일 경로:\n{self.report_path}")
        self.battery_report_button.config(bootstyle="info")
        self.mark_test_complete("배터리")
        # 리포트 이름을 문자열로 저장
        self.report = os.path.basename(self.report_path) if self.report_path else None
        # Django 서버 업로드는 백그라운드에서 진행
        threading.Thread(target=self.upload_battery_report, args=(self.report_path,)).start()

    def _on_battery_report_error(self, error_message: str) -> None:
        """
        배터리 리포트 생성 중 오류 발생 시 실행되는 콜백 메서드 (메인 스레드에서 실행)
        """
        messagebox.showerror("배터리 리포트 오류", error_message)
        self.update_status("배터리", "오류 발생")

    def upload_battery_report(self, report_path):
        """
        (기존) S3 직접 업로드 -> (변경) Django 서버로 업로드
        """
        # Django 서버 endpoint
        # 실제 주소/포트를 맞춰서 기입: 예) https://j12d101.p.ssafy.io/s3app/upload_battery/
        django_url = "https://j12d101.p.ssafy.io/django/s3app/upload_battery/"
        # django_url = "http://localhost:8000/s3app/upload_battery/"

        try:
            with open(report_path, 'rb') as f:
                # 파일 전송
                files = {'file': (os.path.basename(report_path), f, 'text/html')}
                resp = requests.post(django_url, files=files)

                if resp.status_code == 200:
                    data = resp.json()
                    messagebox.showinfo("업로드 완료", f"Django 업로드 성공(파일을 s3에 업로드)")
                else:
                    messagebox.showerror("업로드 오류", f"status={resp.status_code}, body={resp.text}")
        except Exception as e:
            messagebox.showerror("업로드 예외", f"Django 서버 업로드 중 오류 발생: {e}")

    def view_battery_report(self) -> None:
        """
        생성된 배터리 리포트 파일을 엽니다.
        """
        if self.report_path and os.path.exists(self.report_path):
            try:
                os.startfile(self.report_path)
            except Exception as e:
                messagebox.showerror("리포트 확인 오류", f"리포트를 열 수 없습니다:\n{e}")
                self.update_status("배터리", "오류 발생")

        else:
            messagebox.showwarning("리포트 없음", "아직 배터리 리포트가 생성되지 않았습니다.\n먼저 '배터리 리포트 생성' 버튼을 눌러주세요.")
            self.update_status("배터리", "생성 전")

    def summary_battery_report(self) -> None:
        """
        배터리 리포트를 요약하여 중요 정보를 추출합니다.
        - 현재 배터리 용량
        - 설계 용량 
        - 배터리 상태
        - 배터리 수명 예측
        """
        # 리포트 파일 경로가 없거나 파일이 존재하지 않으면 경고 메시지 출력
        if not self.report_path or not os.path.exists(self.report_path):
            messagebox.showwarning("리포트 없음", "배터리 리포트가 존재하지 않습니다.")
            return

        try:
            # 파일을 읽어 실제 HTML 콘텐츠를 가져옴
            with open(self.report_path, 'r', encoding='utf-8') as f:
                content = f.read()

            summary = {}

            # 실제 파일 구조에 맞게 정규 표현식을 수정하여 배터리 정보 추출
            # DESIGN CAPACITY: 숫자에는 쉼표가 포함될 수 있고, "mWh" 단위가 있음 (예: 79,844 mWh)
            design_capacity = re.search(r'DESIGN CAPACITY.*?([\d,]+)\s*mWh', content, re.DOTALL | re.IGNORECASE)
            # FULL CHARGE CAPACITY: 숫자에는 쉼표가 포함될 수 있고, "mWh" 단위가 있음 (예: 71,750 mWh)
            full_charge_capacity = re.search(r'FULL CHARGE CAPACITY.*?([\d,]+)\s*mWh', content, re.DOTALL | re.IGNORECASE)
            # CYCLE COUNT: 단순 숫자 (예: 217)
            cycle_count = re.search(r'CYCLE COUNT.*?(\d+)', content, re.DOTALL | re.IGNORECASE)

            if design_capacity and full_charge_capacity:
                # 추출한 숫자에서 쉼표 제거 후 정수로 변환
                design_cap = int(design_capacity.group(1).replace(',', ''))
                current_cap = int(full_charge_capacity.group(1).replace(',', ''))
                health_percentage = (current_cap / design_cap) * 100

                summary['design_capacity'] = design_cap
                summary['current_capacity'] = current_cap
                summary['health_percentage'] = round(health_percentage, 2)
                summary['cycle_count'] = int(cycle_count.group(1)) if cycle_count else "N/A"

                # 배터리 상태 판단
                if health_percentage >= 80:
                    summary['status'] = "양호"
                elif health_percentage >= 60:
                    summary['status'] = "주의"
                else:
                    summary['status'] = "교체 필요"

                # 배터리 수명 예측 (단순화된 선형 예측: 최대 500회 충전 기준)
                remaining_cycles = 500 - (summary['cycle_count'] if isinstance(summary['cycle_count'], int) else 0)
                if remaining_cycles > 0:
                    summary['life_expectancy'] = f"약 {remaining_cycles}회 충전 가능"
                else:
                    summary['life_expectancy'] = "수명 초과"

                # # 서버에 배터리 상태 전송 (실제 리포트 내용을 detail에 포함)
                # self.send_test_result(
                #     test_type="배터리",
                #     success=True,
                #     detail=content,  # 실제 파일 내용 전달 (&#8203;:contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1})
                #     summary=summary
                # )

                # 사용자에게 결과 표시
                message = (
                    f"배터리 상태 요약:\n\n"
                    f"설계 용량: {summary['design_capacity']} mWh\n"
                    f"현재 용량: {summary['current_capacity']} mWh\n"
                    f"배터리 수명: {summary['health_percentage']}%\n"
                    f"충전 횟수: {summary['cycle_count']}\n"
                    f"배터리 상태: {summary['status']}\n"
                    f"예상 수명: {summary['life_expectancy']}"
                )
                messagebox.showinfo("배터리 요약", message)
                return message
            else:
                raise ValueError("배터리 정보를 찾을 수 없습니다.")

        except Exception as e:
            messagebox.showerror("요약 오류", f"배터리 리포트 요약 중 오류 발생:\n{e}")

    # -------------------------------
    # 서버로 테스트 결과 전송 관련 메서드
    # -------------------------------
    def send_test_result(self, test_type: str, success: bool, detail: str = None, summary:str = None) -> bool:
        """
        테스트 결과를 서버에 전송합니다.
        Args:
            test_type: 테스트 유형 ("키보드", "카메라", "USB", "충전", "배터리")
            success: 테스트 성공 여부
            detail: 상세 정보 (문자열)
        Returns:
            bool: 전송 성공 여부
        """
        try:
            url = "http://localhost:8080/api/test-result"  # 테스트 서버 URL
            # url = "https://j12d101.p.ssafy.io/api/test-result"  # 운영 서버 URL
            
            if test_type == '배터리':
                data = {
                    "randomKey": self.random_key,
                    "testType": test_type,
                    "success": success,
                    "detail": detail if isinstance(detail, str) else str(detail) if detail else None,
                    "summary": self.summary_battery_report()
                }
            else:
                # USB와 키보드의 경우 리스트를 문자열로 변환
                if isinstance(detail, list):
                    detail = ','.join(map(str, detail))
                    
                data = {
                    "randomKey": self.random_key,
                    "testType": test_type,
                    "success": success,
                    "detail": detail if not success else None
                }
                
            response = requests.post(url, json=data)

            if response.status_code == 200:
                messagebox.showinfo("테스트 결과 전송", f"{test_type} 테스트 결과가 서버에 전송되었습니다.")
                return True
            else:
                messagebox.showerror("테스트 결과 전송 오류", f"서버 응답 오류: {response.status_code}")
                return False
                
        except requests.RequestException as e:
            messagebox.showerror("테스트 결과 전송 오류", f"서버와의 통신 오류: {e}")
            return False
        
    # -------------------------------
    # QR 코드 생성 관련 메서드
    # -------------------------------
    def generate_qr_code(self) -> None:
        """
        테스트 결과를 JSON 형식으로 구성 후 QR 코드를 생성하여 표시합니다.
        """
        
        if len(self.test_list) == 0:
            results = {
                "keyboard": {
                    "status": "pass" if self.test_done.get("키보드") else "fail",
                    "failed_keys": sorted(self.failed_keys) if not self.test_done.get("키보드") else []
                },
                "usb": {
                    "status": "pass" if self.test_done.get("USB") else "fail",
                    "failed_ports": [port for port, connected in self.usb_ports.items() if not connected]
                },
                "camera": {
                    "status": "pass" if self.test_done.get("카메라") else "fail"
                },
                "charger": {
                    "status": "pass" if self.test_done.get("충전") else "fail"
                },
                "battery_report": {
                    "status": "pass" if self.report_path and os.path.exists(self.report_path) else "fail",
                    "reportName": os.path.basename(self.report_path) if self.report_path else None,
                }
            }
            qr_data = json.dumps(results, ensure_ascii=False, indent=2)
            try:
                qr = qrcode.QRCode(
                    version=None,
                    error_correction=qrcode.constants.ERROR_CORRECT_L,
                    box_size=4,
                    border=4,
                )
                qr.add_data(qr_data)
                qr.make(fit=True)
                img = qr.make_image(fill_color="black", back_color="white")
                qr_img = ImageTk.PhotoImage(img)
                qr_window = ttkb.Toplevel(self)
                qr_window.title("상세 테스트 결과 QR 코드")
                qr_label = ttkb.Label(qr_window, image=qr_img)
                qr_label.image = qr_img  # 이미지 참조 유지
                qr_label.pack(padx=10, pady=10)
                self.mark_test_complete("QR코드")
            except Exception as e:
                messagebox.showerror("QR 코드 생성 오류", f"QR 코드 생성 중 오류 발생:\n{e}")
        else:
            messagebox.showerror("QR 코드 생성 오류", f"테스트를 진행하지 않아 QR코드를 생성할 수 없습니다. \n 남은 테스트 목록{self.test_list}")
            self.update_status("QR코드", "오류 발생")

# ===============================
# 애플리케이션 실행
# ===============================
if __name__ == "__main__":
    app = TestApp()
    app.mainloop()