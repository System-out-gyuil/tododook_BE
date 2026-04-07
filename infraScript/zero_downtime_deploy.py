#!/usr/bin/env python3

import os
import requests
import subprocess
import time
from datetime import datetime
from typing import Dict, Optional


def log(msg: str) -> None:
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}", flush=True)

DOCKER_IMAGE_NAME = "tododook"
GITHUB_ID = "system-out-gyuil"
SPRING_PROD_PORT = "8090"

class ServiceManager:
    # 초기화 함수
    def __init__(self, socat_port: int = 8081, sleep_duration: int = 3) -> None:
        self.socat_port: int = socat_port
        self.sleep_duration: int = sleep_duration
        self.services: Dict[str, int] = {
            f'{DOCKER_IMAGE_NAME}_1': 8082,
            f'{DOCKER_IMAGE_NAME}_2': 8083
        }
        self.current_name: Optional[str] = None
        self.current_port: Optional[int] = None
        self.next_name: Optional[str] = None
        self.next_port: Optional[int] = None

    # 현재 실행 중인 서비스를 찾는 함수
    def _find_current_service(self) -> None:
        cmd: str = f"ps aux | grep 'socat -t0 TCP-LISTEN:{self.socat_port}' | grep -v grep | awk '{{print $NF}}'"
        log(f"[find_current] socat 감지 명령어: {cmd}")

        ps_raw: str = subprocess.getoutput(f"ps aux | grep 'socat -t0 TCP-LISTEN:{self.socat_port}' | grep -v grep")
        log(f"[find_current] ps aux grep 원본 결과:\n{ps_raw if ps_raw else '(없음)'}")

        current_service: str = subprocess.getoutput(cmd)
        log(f"[find_current] awk 추출 결과: '{current_service}'")

        if not current_service:
            log(f"[find_current] socat 감지 실패 → 기본값 사용: current={DOCKER_IMAGE_NAME}_2 (port=8083)")
            self.current_name, self.current_port = f'{DOCKER_IMAGE_NAME}_2', self.services[f'{DOCKER_IMAGE_NAME}_2']
        else:
            self.current_port = int(current_service.split(':')[-1])
            self.current_name = next((name for name, port in self.services.items() if port == self.current_port), None)
            log(f"[find_current] socat 감지 성공 → current={self.current_name} (port={self.current_port})")

    # 다음에 실행할 서비스를 찾는 함수
    def _find_next_service(self) -> None:
        self.next_name, self.next_port = next(
            ((name, port) for name, port in self.services.items() if name != self.current_name),
            (None, None)
        )
        log(f"[find_next] next={self.next_name} (port={self.next_port})")

    # Docker 컨테이너를 제거하는 함수
    def _remove_container(self, name: str) -> None:
        os.system(f"docker stop {name} 2> /dev/null")
        os.system(f"docker rm -f {name} 2> /dev/null")

    # Docker 컨테이너를 실행하는 함수
    def _run_container(self, name: str, port: int) -> None:
        os.system(f"docker pull ghcr.io/{GITHUB_ID}/{DOCKER_IMAGE_NAME}:latest")

        cmd = f"""
        docker run -d --name={name} \
        --restart unless-stopped \
        -p {port}:{SPRING_PROD_PORT} \
        -e TZ=Asia/Seoul \
        -v /{DOCKER_IMAGE_NAME}/volumes/gen:/gen \
        ghcr.io/{GITHUB_ID}/{DOCKER_IMAGE_NAME}
        """
        result = os.system(cmd)
        if result != 0:
            raise Exception("Docker run failed")

    def _switch_port(self) -> None:
        cmd: str = f"ps aux | grep 'socat -t0 TCP-LISTEN:{self.socat_port}' | grep -v grep | awk '{{print $2}}'"
        pid: str = subprocess.getoutput(cmd)
        log(f"[switch_port] 기존 socat PID 조회 결과: '{pid}'")

        if pid:
            log(f"[switch_port] 기존 socat 종료 시도: kill -9 {pid}")
            ret = os.system(f"kill -9 {pid} 2>/dev/null")
            log(f"[switch_port] kill 반환코드: {ret}")
        else:
            log("[switch_port] 종료할 socat 프로세스 없음")

        time.sleep(5)

        socat_cmd = f"bash -c 'nohup socat -t0 TCP-LISTEN:{self.socat_port},fork,reuseaddr TCP:localhost:{self.next_port} >/dev/null 2>&1 &'"
        log(f"[switch_port] 새 socat 실행 명령어: {socat_cmd}")
        ret = os.system(socat_cmd)
        log(f"[switch_port] socat 실행 반환코드: {ret}")

        time.sleep(1)
        verify: str = subprocess.getoutput(f"ps aux | grep 'socat -t0 TCP-LISTEN:{self.socat_port}' | grep -v grep")
        log(f"[switch_port] 실행 후 socat 프로세스 확인:\n{verify if verify else '(없음 - socat 실행 실패!)'}")

        # 서비스 상태를 확인하는 함수

    def _is_service_up(self, port: int) -> bool:
        url = f"http://127.0.0.1:{port}/actuator/health"
        try:
            response = requests.get(url, timeout=5)  # 5초 이내 응답 없으면 예외 발생
            if response.status_code == 200 and response.json().get('status') == 'UP':
                return True
        except requests.RequestException:
            pass
        return False

    # 서비스를 업데이트하는 함수
    def update_service(self) -> None:
        log("========== 배포 시작 ==========")

        running_containers = subprocess.getoutput("docker ps --format '{{.Names}}\t{{.Ports}}'")
        log(f"[update] 현재 실행 중인 컨테이너:\n{running_containers if running_containers else '(없음)'}")

        self._find_current_service()
        self._find_next_service()

        log(f"[update] next 컨테이너 제거: {self.next_name}")
        self._remove_container(self.next_name)

        log(f"[update] next 컨테이너 실행: {self.next_name} (port={self.next_port})")
        self._run_container(self.next_name, self.next_port)

        while not self._is_service_up(self.next_port):
            log(f"[update] {self.next_name} 헬스체크 대기 중...")
            time.sleep(self.sleep_duration)

        log(f"[update] {self.next_name} 헬스체크 통과")
        self._switch_port()

        if self.current_name is not None:
            log(f"[update] 기존 컨테이너 제거: {self.current_name}")
            self._remove_container(self.current_name)

        log("========== 배포 완료 ==========")


if __name__ == "__main__":
    manager = ServiceManager()
    manager.update_service()