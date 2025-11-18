import { useEffect } from "preact/hooks";
import { createRef } from "preact";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "./components/ui/table";
import { v4 as uuidv4 } from 'uuid';
import type { Cell, Row, TableDataProps } from "./model";

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

export default function TableData(props: TableDataProps) {
  const bodyRef = createRef();

  useEffect(() => {
    if (bodyRef.current) {
      let offset = props.isDetailsOpen ? 390 : '285'
      bodyRef.current.style.height = `calc(100vh - ${offset}px)`;
    }
  }, [props.isDetailsOpen]);

  return (
    <>
      <div ref={bodyRef} className="flex flex-col px-4">
        <Table>
          <TableHeader className="sticky top-0 bg-muted">
            <TableRow>
              {headers.map((h: any) =>
                <TableHead key={h.title}>{h.title}</TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody className="overflow-y-auto">
            {props.data.map((row: Row) => (
              <TableRow
                key={uuidv4()}
                className={row.cells[7].value > 0 ? '!bg-input' : 'odd:bg-background even:bg-header'}
              >
                {row.cells.map((cell: Cell) => (
                  <TableCell className="font-medium">{cell.value}</TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </>
  );
}