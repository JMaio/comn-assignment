stats = {
    1: [
        6.70,
        4.83,
        5.65,
        6.32,
        5.71,
    ],
    2: [
        9.44,
        5.51,
        5.92,
        6.04,
        5.67,
    ],
    4: [
        10.8,
        13.5,
        17.0,
        11.8,
        16.2,
    ],
    8: [
        19.2,
        19.8,
        18.4,
        15.7,
        17.3,
    ],
    16: [
        21.1,
        16.3,
        17.2,
        12.6,
        20.4,
    ],
    32: [
        20.0,
        20.1,
        17.9,
        21.5,
        20.8,
    ],
}

for (tx_k, tx_vs) in stats.items():
    r = tx_vs
    print(tx_k)
    print(f"{round(sum(r) / len(r), 2)}")
    print()