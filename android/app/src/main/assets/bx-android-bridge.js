(function () {
  if (window.BetterXcloudAndroidBridge) {
    window.BetterXcloudNative?.onBridgeReady?.();
    return;
  }

  const BUTTON_INDEX = {
    a: 0,
    b: 1,
    x: 2,
    y: 3,
    lb: 4,
    rb: 5,
    lt: 6,
    rt: 7,
    view: 8,
    menu: 9,
    ls: 10,
    rs: 11,
    dpad_up: 12,
    dpad_down: 13,
    dpad_left: 14,
    dpad_right: 15
  };

  const nativeState = {
    connected: false,
    mapping: Object.assign({}, BUTTON_INDEX),
    buttons: Object.keys(BUTTON_INDEX).reduce((acc, key) => {
      acc[key] = { pressed: false, value: 0 };
      return acc;
    }, {}),
    axes: {
      lx: 0,
      ly: 0,
      rx: 0,
      ry: 0,
      lt: 0,
      rt: 0
    },
    timestamp: performance.now()
  };

  const originalGetGamepads = navigator.getGamepads?.bind(navigator);

  function buildGamepad() {
    if (!nativeState.connected) {
      return null;
    }
    const buttons = new Array(16).fill(0).map(() => ({ pressed: false, value: 0 }));
    Object.entries(nativeState.buttons).forEach(([key, entry]) => {
      const index = BUTTON_INDEX[key];
      if (index == null) return;
      buttons[index] = { pressed: entry.pressed, value: entry.value };
    });
    const axes = [
      nativeState.axes.lx,
      nativeState.axes.ly,
      nativeState.axes.rx,
      nativeState.axes.ry
    ];
    return {
      id: "BetterXcloud Android Controller",
      index: 0,
      connected: true,
      mapping: "standard",
      buttons,
      axes,
      timestamp: nativeState.timestamp,
      vibrationActuator: null
    };
  }

  function dispatchGamepadEvent(type) {
    const gamepad = buildGamepad();
    if (!gamepad) return;
    let event;
    if (typeof GamepadEvent === "function") {
      event = new GamepadEvent(type, { gamepad });
    } else {
      event = new CustomEvent(type, { detail: { gamepad } });
    }
    window.dispatchEvent(event);
  }

  function refreshGamepadOverride() {
    const fallback = function () {
      if (!nativeState.connected) {
        return originalGetGamepads ? originalGetGamepads() : [];
      }
      const base = originalGetGamepads ? originalGetGamepads() : [];
      const length = Math.max(1, base.length);
      const result = new Array(length).fill(null);
      result[0] = buildGamepad();
      for (let i = 1; i < length; i++) {
        result[i] = base[i];
      }
      return result;
    };
    navigator.getGamepads = fallback;
  }

  const bridge = {
    readyState: "loading",
    kernel: "native",
    setKernel(kernel) {
      this.kernel = kernel;
      if (kernel === "native") {
        if (!nativeState.connected) {
          nativeState.connected = true;
          nativeState.timestamp = performance.now();
          refreshGamepadOverride();
          dispatchGamepadEvent("gamepadconnected");
        }
      } else {
        if (nativeState.connected) {
          nativeState.connected = false;
          dispatchGamepadEvent("gamepaddisconnected");
        }
        if (originalGetGamepads) {
          navigator.getGamepads = originalGetGamepads;
        }
      }
    },
    updateMapping(mapping) {
      nativeState.mapping = Object.assign({}, mapping);
    },
    onNativeEvent(event) {
      if (this.kernel !== "native") return;
      nativeState.timestamp = event.timestamp || performance.now();
      if (event.type === "button") {
        const buttonKey = event.button;
        const entry = nativeState.buttons[buttonKey];
        if (entry) {
          entry.pressed = !!event.pressed;
          entry.value = entry.pressed ? 1 : 0;
          dispatchGamepadEvent("gamepadbutton");
        }
      } else if (event.type === "axes") {
        nativeState.axes.lx = Number(event.lx || 0);
        nativeState.axes.ly = Number(event.ly || 0);
        nativeState.axes.rx = Number(event.rx || 0);
        nativeState.axes.ry = Number(event.ry || 0);
        nativeState.axes.lt = Number(event.lt || 0);
        nativeState.axes.rt = Number(event.rt || 0);
        nativeState.buttons.lt.pressed = nativeState.axes.lt > 0.2;
        nativeState.buttons.lt.value = Math.min(1, Math.max(0, nativeState.axes.lt));
        nativeState.buttons.rt.pressed = nativeState.axes.rt > 0.2;
        nativeState.buttons.rt.value = Math.min(1, Math.max(0, nativeState.axes.rt));
        dispatchGamepadEvent("gamepadaxes");
      }
    }
  };

  window.BetterXcloudAndroidBridge = bridge;
  bridge.readyState = "complete";
  window.BetterXcloudNative?.onBridgeReady?.();
})();
