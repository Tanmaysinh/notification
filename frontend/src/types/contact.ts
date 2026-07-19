export interface Contact {
  contactId: string;
  name: string;
  email: string;
  phoneNumber: string;
  deviceToken: string | null;
}

export type ContactFormValues = Omit<Contact, "contactId">;