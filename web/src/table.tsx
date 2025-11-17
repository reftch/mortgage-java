import { useEffect } from "preact/hooks";
import { Separator } from "./components/ui/separator";
import Details from "./details";
import { v4 as uuidv4 } from 'uuid';
import { signal } from '@preact/signals';
import './table.css';
import { createRef } from "preact";

type Row = { cells: Array<{ value: number; width: number; minWidth: number }> };

const headers = [
  { title: "Jahr", width: 60 },
  { title: "Monat", width: 80 },
  { title: "Schulden", width: 120 },
  { title: "Zahlung", width: 100 },
  { title: "Zinsen", width: 100 },
  { title: "Kapitalbetrag", width: 100 },
  { title: "Neue Schulden", width: 120 },
  { title: "Einmalige Überzahlung", width: 200 },
];

const data = signal<Array<Row>>();
const overall = signal<number>(0.0);
const isDetailsOpen = signal<boolean>(true);

export default function Table({ amount, rate, overpayment, years }: any) {
  const bodyRef = createRef();

  useEffect(() => {
    if (bodyRef.current) {
      const offset = isDetailsOpen.value ? 455 : '350'
      bodyRef.current.style.height = `calc(100vh - ${offset}px)`;
    }
  }, [isDetailsOpen.value]);

  useEffect(() => {
    data.value = [];
    const months = years * 12;

    let balance = amount;

    const payment = getMonthlyPayment(balance, rate, months);

    let irPaid, amountPaid, newBalance;
    overall.value = 0.0;

    for (let month = 1; month <= months; month++) {
      irPaid = balance * rate;
      amountPaid = payment - irPaid;
      newBalance = balance - amountPaid;

      if (balance - payment <= 0) {
        break;
      }

      let addPayment = 0.0;

      const year = month % 12;
      if (year == 0) {
        addPayment = balance * overpayment;
        overall.value += addPayment;
      }

      const row: Row = {
        cells: [
          { value: Math.floor(month / 12) + 1, width: 60,  minWidth: 60 },
          { value: month, width: 80,  minWidth: 80 },
          { value: Number(balance.toFixed(0)), width: 120,  minWidth: 120 },
          { value: Number(payment.toFixed(2)), width: 100,  minWidth: 100 },
          { value: Number(irPaid.toFixed(2)), width: 100,  minWidth: 100 },
          { value: Number(amountPaid.toFixed(2)), width: 100,  minWidth: 100 },
          { value: Number(newBalance.toFixed(0)), width: 120,  minWidth: 120 },
          { value: Number(addPayment.toFixed(0)), width: 200, minWidth: 100 },
        ],
      };
      data.value.push(row);

      // update balance
      balance = newBalance - addPayment;
      // calculate overall payment
      overall.value += payment;
    }
  }, [amount, rate, years, overpayment]);

  const getMonthlyPayment = (amount: number, rate: number, months: number) => {
    return (rate * amount) / (Math.pow(1 + rate, -months));
  };

  const renderHeader = () => {
    return (
      <thead>
        <tr>
          {headers.map((h: any) => (
            <th
              className="header"
              key={h.title}
              style={{ width: `${h.width}px` }}
            >
              <div className="text-block111" >
                {h.title}
              </div>
            </th>
          ))}
        </tr>
      </thead>
    );
  };

  const renderRow = (row: Row) => {
    return (
      <tr key={uuidv4()} className={row.cells[7].value > 0 ? "selected" : ""}>
        {row.cells.map((cell) => (
          <td
            key={uuidv4()}
            style={{ width: `${cell.width}px` }}
          >
            {cell.value}
          </td>
        ))}
      </tr>
    );
  };

  const renderBody = () => {
    return (
      <tbody ref={bodyRef} className="body-scroll">
        {data.value?.map((row) => renderRow(row))}
      </tbody>
    );
  };

  return (
    <>
      <Details isOpen={isDetailsOpen} overall={overall.value} amount={amount} onChange={(data: any) => isDetailsOpen.value = data} />
      <Separator />
      <div className="flex flex-col px-4">
        <table>
          {renderHeader()}
          {renderBody()}
        </table>
      </div>
    </>
  );
}